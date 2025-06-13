package com.jzo2o.market.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.*;
import com.jzo2o.market.constants.TabTypeConstants;
import com.jzo2o.market.enums.ActivityStatusEnum;
import com.jzo2o.market.mapper.ActivityMapper;
import com.jzo2o.market.mapper.CouponMapper;
import com.jzo2o.market.model.domain.Activity;
import com.jzo2o.market.model.domain.CouponWriteOff;
import com.jzo2o.market.model.dto.request.ActivityQueryForPageReqDTO;
import com.jzo2o.market.model.dto.request.ActivitySaveReqDTO;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;
import com.jzo2o.market.model.dto.response.CountResDTO;
import com.jzo2o.market.model.dto.response.SeizeCouponInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.market.service.ICouponWriteOffService;
import com.jzo2o.mysql.utils.PageUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jzo2o.market.constants.RedisConstants.RedisKey.*;
import static com.jzo2o.market.enums.ActivityStatusEnum.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements IActivityService {
    private static final int MILLION = 1000000;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ICouponService couponService;

    @Resource
    private ICouponWriteOffService couponWriteOffService;


    /**
     * 新增或修改一个优惠券活动
     * @param dto
     */
    @Override
    public void saveOrUpdateActivity(ActivitySaveReqDTO dto) {
        //0. 校验
        dto.check();

        //1. 将dto转换为实体类对象, (状态和库存赋值默认值)
        Activity activity = BeanUtil.copyProperties(dto, Activity.class);
        activity.setStatus(NO_DISTRIBUTE.getStatus());//待生效
        activity.setStockNum(activity.getTotalNum());//库存,一开始等于发放总数量

        //2. 调用service保存
        this.saveOrUpdate(activity);
    }

    /**
     * 运营端分页查询优惠券活动
     * @param dto
     * @return
     */
    @Autowired
    private CouponMapper couponMapper;


    @Override
    public PageResult<ActivityInfoResDTO> findByPage(ActivityQueryForPageReqDTO dto) {
        //1. 设置分页条件
        Page<Activity> page = PageUtils.parsePageQuery(dto, Activity.class);

        //2. 设置业务条件
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getId() != null, Activity::getId, dto.getId())
                .like(StringUtils.isNotEmpty(dto.getName()), Activity::getName, dto.getName())
                .eq(dto.getType() != null, Activity::getType, dto.getType())
                .eq(dto.getStatus() != null, Activity::getStatus, dto.getStatus());

        //3. 执行分页查询
        page = this.page(page, wrapper);
        if (CollUtil.isEmpty(page.getRecords())) {
            return new PageResult<ActivityInfoResDTO>(page.getPages(), page.getTotal(), List.of());
        }

        //4. 提前查询本页所有活动id对应的优惠券领取数量, 封装到一个Map<活动id,优惠券领取数量>集合中备用
        // 4-1 提前收集到当前页所有活动的id到一个集合中
        List<Long> activityIdList
                = page.getRecords().stream().map(Activity::getId).collect(Collectors.toList());

        //4-2 调用mapper根据优惠券活动id集合查询优惠券领取数量集合
        List<CountResDTO> countResDTOList = couponMapper.countByActivityIdList(activityIdList);

        //4-3 将上述集合转换为map备用
        Map<Long, Integer> receiveNumMap = countResDTOList.stream().collect(Collectors.toMap(CountResDTO::getActivityId, CountResDTO::getNum));

        //5. 组装返回结果
        List<ActivityInfoResDTO> list = page.getRecords().stream().map(e -> {
            ActivityInfoResDTO activityInfoResDTO = BeanUtil.copyProperties(e, ActivityInfoResDTO.class);

            //优惠券领取数量  活动id
            //select count(*) from coupon where activity_id = 活动id
            //Integer count1 = couponService.lambdaQuery().eq(Coupon::getActivityId, e.getId()).count();
            activityInfoResDTO.setReceiveNum(receiveNumMap.getOrDefault(e.getId(),0));

            //优惠券核销数量
            //select count(*) from coupon_write_off where activity_id = 活动id
            Integer count2 = couponWriteOffService.lambdaQuery().eq(CouponWriteOff::getActivityId, e.getId()).count();
            activityInfoResDTO.setWriteOffNum(count2);

            return activityInfoResDTO;
        }).collect(Collectors.toList());
        return new PageResult<ActivityInfoResDTO>(page.getPages(), page.getTotal(), list);
    }
}
