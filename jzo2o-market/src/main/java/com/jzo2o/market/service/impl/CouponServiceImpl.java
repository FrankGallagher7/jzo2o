package com.jzo2o.market.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.db.DbRuntimeException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.market.dto.request.CouponUseBackReqDTO;
import com.jzo2o.api.market.dto.request.CouponUseReqDTO;
import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.api.market.dto.response.CouponUseResDTO;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.expcetions.DBException;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.*;
import com.jzo2o.market.enums.ActivityStatusEnum;
import com.jzo2o.market.enums.ActivityTypeEnum;
import com.jzo2o.market.enums.CouponStatusEnum;
import com.jzo2o.market.mapper.CouponMapper;
import com.jzo2o.market.model.domain.Activity;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.domain.CouponWriteOff;
import com.jzo2o.market.model.dto.request.CouponOperationPageQueryReqDTO;
import com.jzo2o.market.model.dto.request.SeizeCouponReqDTO;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.market.service.ICouponUseBackService;
import com.jzo2o.market.service.ICouponWriteOffService;
import com.jzo2o.market.utils.CouponUtils;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.mysql.utils.PageUtils;
import com.jzo2o.redis.utils.RedisSyncQueueUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.jzo2o.common.constants.ErrorInfo.Code.SEIZE_COUPON_FAILD;
import static com.jzo2o.market.constants.RedisConstants.RedisKey.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
@Service
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {

    @Resource(name = "seizeCouponScript")
    private DefaultRedisScript<String> seizeCouponScript;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private IActivityService activityService;

    @Resource
    private ICouponUseBackService couponUseBackService;

    @Resource
    private ICouponWriteOffService couponWriteOffService;


    /**
     * 查询优惠券领取记录
     * @param dto
     * @return
     */
    @Override
    public PageResult<CouponInfoResDTO> findByPage(CouponOperationPageQueryReqDTO dto) {
        //0. 校验
        if (dto.getActivityId() == null) {
            throw new ForbiddenOperationException("请指定活动id");
        }

        //1. 设置分页条件
        Page<Coupon> page = PageUtils.parsePageQuery(dto, Coupon.class);

        //2. 设置业务条件
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getActivityId, dto.getActivityId());

        //3. 执行分页
        page = this.page(page,wrapper);

        //4. 组装返回结果
        return PageUtils.toPage(page,CouponInfoResDTO.class);
    }

    /**
     * 已领取优惠券自动过期任务
     */
    @Override
    public void processExpireCoupon() {
        lambdaUpdate()
                .eq(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus())//未使用
                .le(Coupon::getValidityTime, DateUtils.now())//有效期小于当前时间
                .set(Coupon::getStatus, CouponStatusEnum.INVALID.getStatus())//已失效
                .update();
    }

    //抢券
    @Override
    public void seizeCoupon(SeizeCouponReqDTO seizeCouponReqDTO) {
        // 1.校验当前时间是否在活动期间
        ActivityInfoResDTO activity = activityService.getActivityInfoByIdFromCache(seizeCouponReqDTO.getId());
        if (activity == null || activity.getDistributeStartTime().isAfter(LocalDateTime.now())) {
            throw new CommonException(SEIZE_COUPON_FAILD, "活动不存在或者未开始");
        }
        if (activity.getDistributeEndTime().isBefore(LocalDateTime.now())) {
            throw new CommonException(SEIZE_COUPON_FAILD, "活动不存在或者已结束");
        }

        //2. 抢券
        //2-1 准备参数
        int index = (int) (seizeCouponReqDTO.getId() % 10);
        String couponSeizeSyncRedisKey = RedisSyncQueueUtils.getQueueRedisKey(COUPON_SEIZE_SYNC_QUEUE_NAME, index);// 同步队列redisKey
        String resourceStockRedisKey = String.format(COUPON_RESOURCE_STOCK, index);// 资源库存redisKey
        String couponSeizeListRedisKey = String.format(COUPON_SEIZE_LIST, activity.getId(), index);// 抢券列表
        log.debug("seize coupon keys -> couponSeizeListRedisKey->{},resourceStockRedisKey->{},couponSeizeListRedisKey->{},seizeCouponReqDTO.getId()->{},UserContext.currentUserId():{}",
                couponSeizeListRedisKey, resourceStockRedisKey, couponSeizeListRedisKey, seizeCouponReqDTO.getId(), UserContext.currentUserId());

        //2-2 执行抢券脚本
        Object executeResult = redisTemplate.execute(
                seizeCouponScript, //脚本
                Arrays.asList(couponSeizeSyncRedisKey, resourceStockRedisKey, couponSeizeListRedisKey),//键
                seizeCouponReqDTO.getId(), UserContext.currentUserId()//参数
        );

        //3. 返回结果
        if (executeResult == null) {
            throw new CommonException(SEIZE_COUPON_FAILD, "抢券失败");
        }
        long result = NumberUtils.parseLong(executeResult.toString());
        if (result > 0) {
            return; //成功
        }else if (result == -1) {
            throw new CommonException(SEIZE_COUPON_FAILD, "限领一张");
        }else if (result == -2 || result == -4) {
            throw new CommonException(SEIZE_COUPON_FAILD, "已抢光!");
        }else{
            throw new CommonException(SEIZE_COUPON_FAILD, "抢券失败");
        }
    }

    /**
     * 用户端查询我的优惠券列表
     * @param lastId
     * @param userId
     * @param status
     * @return
     */
    @Override
    public List<CouponInfoResDTO> queryForList(Long lastId, Long userId, Integer status) {
            List<Coupon> list = this.lambdaQuery()
                    .eq(Coupon::getStatus, status)
                    .eq(Coupon::getUserId, userId)
                    .lt(lastId != null, Coupon::getId, lastId)
                    .orderByDesc(Coupon::getCreateTime)
                    .last("limit 10")
                    .list();
            return BeanUtils.copyToList(list, CouponInfoResDTO.class);
    }

    /**
     * 获取可用优惠券列表（微服务调用）
     * @param totalAmount
     * @return
     */
    @Override
    public List<AvailableCouponsResDTO> getAvailable(BigDecimal totalAmount) {
    // 1.查询可用优惠券
    List<Coupon> coupons = this.lambdaQuery()
                .eq(Coupon::getUserId, UserContext.currentUserId()) //当前用户
                .eq(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus()) //未使用
                .ge(Coupon::getValidityTime, LocalDateTime.now()) //有效期大于当前时间
                .le(Coupon::getAmountCondition, totalAmount) //优惠金额小于等于订单金额
                .list();

    // 2.计算优惠金额   满减  折扣
    if (CollectionUtil.isEmpty(coupons)) {
        return List.of();
    }
        List<Coupon> couponList = coupons.stream()
                // 计算优惠金额
                .map(coupon -> coupon.setDiscountAmount(CouponUtils.calDiscountAmount(coupon, totalAmount)))
                // 过滤掉优惠金额小于订单金额的优惠券
                .filter(coupon -> coupon.getDiscountAmount().compareTo(totalAmount) < 0 && coupon.getDiscountAmount()
                        .compareTo(new BigDecimal(0)) > 0).sorted(Comparator.comparing(Coupon::getDiscountAmount)
                        .reversed()).collect(Collectors.toList());

        return BeanUtil.copyToList(couponList,AvailableCouponsResDTO.class);
    }

    /**
     * 核销优惠券
     * @param couponUseReqDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponUseResDTO use(CouponUseReqDTO couponUseReqDTO) {
        //1. 校验优惠券信息: 只有订单金额大于等于满减金额，并且优惠券在有效状态方可使用
        Coupon coupon = this.lambdaQuery()
                .eq(Coupon::getUserId, UserContext.currentUserId())//- 所属用户：当前登录用户
                .eq(Coupon::getStatus, CouponStatusEnum.NO_USE.getStatus())//- 状态：未使用
                .ge(Coupon::getValidityTime, LocalDateTime.now())//- 在有效使用期限内
                .le(Coupon::getAmountCondition, couponUseReqDTO.getTotalAmount())//- 满减金额：小于等于订单总额
                .eq(Coupon::getId, couponUseReqDTO.getId())//优惠券id
                .one();
        if (ObjectUtil.isNull(coupon)) {
            throw new ForbiddenOperationException("优惠券核销失败");
        }

        //2. 修改优惠券表中该优惠券的使用状态（已使用）、使用时间（当前时间）、订单id（订单微服务传入）
        coupon.setStatus(CouponStatusEnum.USED.getStatus());//使用状态（已使用）
        coupon.setUseTime(LocalDateTime.now());//使用时间（当前时间）
        coupon.setOrdersId(couponUseReqDTO.getOrdersId().toString());//订单id（订单微服务传入）
        this.updateById(coupon);

        //3. 向优惠券核销表添加一条记录
        CouponWriteOff couponWriteOff = new CouponWriteOff();
        couponWriteOff.setCouponId(couponUseReqDTO.getId());
        couponWriteOff.setUserId(UserContext.currentUserId());
        couponWriteOff.setOrdersId(couponUseReqDTO.getOrdersId());
        couponWriteOff.setActivityId(coupon.getActivityId());
        couponWriteOff.setWriteOffTime(LocalDateTime.now());
        couponWriteOff.setWriteOffManPhone(coupon.getUserPhone());
        couponWriteOff.setWriteOffManName(coupon.getUserName());
        couponWriteOffService.save(couponWriteOff);

        //4. 核销成功返回最终优惠的金额
        BigDecimal discountAmount = CouponUtils.calDiscountAmount(coupon, couponUseReqDTO.getTotalAmount());
        CouponUseResDTO couponUseResDTO = new CouponUseResDTO();
        couponUseResDTO.setDiscountAmount(discountAmount);
        return couponUseResDTO;
    }
}
