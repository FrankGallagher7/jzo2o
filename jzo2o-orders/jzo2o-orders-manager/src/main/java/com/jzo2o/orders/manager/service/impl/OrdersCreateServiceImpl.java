package com.jzo2o.orders.manager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.customer.AddressBookApi;
import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import com.jzo2o.api.foundations.ServeApi;
import com.jzo2o.api.foundations.dto.response.ServeAggregationResDTO;
import com.jzo2o.api.trade.NativePayApi;
import com.jzo2o.api.trade.dto.request.NativePayReqDTO;
import com.jzo2o.api.trade.dto.response.NativePayResDTO;
import com.jzo2o.api.trade.enums.PayChannelEnum;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.utils.DateUtils;
import com.jzo2o.common.utils.StringUtils;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.orders.base.constants.RedisConstants;
import com.jzo2o.orders.base.mapper.OrdersMapper;
import com.jzo2o.orders.base.model.domain.Orders;
import com.jzo2o.orders.manager.model.dto.request.OrdersPayReqDTO;
import com.jzo2o.orders.manager.model.dto.request.PlaceOrderReqDTO;
import com.jzo2o.orders.manager.model.dto.response.OrdersPayResDTO;
import com.jzo2o.orders.manager.model.dto.response.PlaceOrderResDTO;
import com.jzo2o.orders.manager.porperties.TradeProperties;
import com.jzo2o.orders.manager.service.IOrdersCreateService;
import com.jzo2o.redis.annotations.Lock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 下单服务类
 * </p>
 *
 * @author itcast
 * @since 2023-07-10
 */
@Slf4j
@Service
public class OrdersCreateServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersCreateService {

    /**
     * 用户下单
     * @param placeOrderReqDTO
     * @return
     */
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ServeApi serveApi;

    @Autowired
    private AddressBookApi addressBookApi;
    @Autowired
    private IOrdersCreateService owner;

    @Override
    public PlaceOrderResDTO placeOrder(PlaceOrderReqDTO placeOrderReqDTO) {
        return owner.placeOrder(placeOrderReqDTO,UserContext.currentUserId());
    }

    /**
     * 创建订单-重载--用户Id-
     * @param placeOrderReqDTO
     * @param userId
     * @return
     */
    @Lock(formatter = "ORDERS:CREATE:LOCK:#{userId}:#{placeOrderReqDTO.serveId}", time = 30, waitTime = 1,unlock=false)
    public PlaceOrderResDTO placeOrder(PlaceOrderReqDTO placeOrderReqDTO, Long userId) {
        //1. 调用运营微服务, 根据服务id查询
        ServeAggregationResDTO serveDto = serveApi.findById(placeOrderReqDTO.getServeId());
        if (ObjectUtil.isNull(serveDto) || serveDto.getSaleStatus() != 2) {
            throw new ForbiddenOperationException("服务不存在或者状态有误");
        }

        //2. 调用customer微服务, 根据地址id查询信息
        AddressBookResDTO addressDto = addressBookApi.detail(placeOrderReqDTO.getAddressBookId());
        if (ObjectUtil.isNull(addressDto)) {
            throw new ForbiddenOperationException("服务地址有误");
        }

        //3. 准备Orders实体类对象
        Orders orders = new Orders();
        orders.setId(generateOrderId());//订单id
        orders.setUserId(UserContext.currentUserId());//下单人id
        orders.setServeId(placeOrderReqDTO.getServeId());//服务id

        //运营数据微服务
        orders.setServeTypeId(serveDto.getServeTypeId());//服务类型id
        orders.setServeTypeName(serveDto.getServeTypeName());//服务类型名称
        orders.setServeItemId(serveDto.getServeItemId());//服务项id
        orders.setServeItemName(serveDto.getServeItemName());//服务项名称
        orders.setServeItemImg(serveDto.getServeItemImg());//服务项图片
        orders.setUnit(serveDto.getUnit());//服务单位
        orders.setPrice(serveDto.getPrice());//服务单价
        orders.setCityCode(serveDto.getCityCode());//城市编码

        orders.setOrdersStatus(0);//订单状态: 待支付
        orders.setPayStatus(2);//支付状态: 待支付

        orders.setPurNum(placeOrderReqDTO.getPurNum());//购买数量
        orders.setTotalAmount(serveDto.getPrice().multiply(new BigDecimal(placeOrderReqDTO.getPurNum())));//总金额: 价格 * 购买数量
        orders.setDiscountAmount(new BigDecimal(0));//优惠金额
        orders.setRealPayAmount(orders.getTotalAmount().subtract(orders.getDiscountAmount()));//实付金额 订单总金额 - 优惠金额

        //地址
        orders.setServeAddress(addressDto.getAddress());//服务详细地址
        orders.setContactsPhone(addressDto.getPhone());//联系人手机号
        orders.setContactsName(addressDto.getName());//联系人名字
        orders.setLon(addressDto.getLon());//经度
        orders.setLat(addressDto.getLat());//纬度

        orders.setServeStartTime(placeOrderReqDTO.getServeStartTime());//服务开始时间
        orders.setDisplay(1);//用户端是否展示 1 展示
        orders.setSortBy(DateUtils.toEpochMilli(placeOrderReqDTO.getServeStartTime()) + orders.getId() % 100000);//排序字段


        //4. 保存到数据表
        owner.saveOrder(orders);

        //5.返回
        return new PlaceOrderResDTO(orders.getId());
    }

    // 保存订单方法
    @Transactional
    public void saveOrder(Orders orders) {
        this.save(orders);
    }
    @Autowired
    private TradeProperties tradeProperties;
    @Autowired
    private NativePayApi nativePayApi;

    /**
     * 订单支付
     * @param id
     * @param ordersPayReqDTO
     * @return
     */
    @Override
    public OrdersPayResDTO pay(Long id, OrdersPayReqDTO ordersPayReqDTO) {
        //调用支付微服务 获取 二维码图片
        //1. 根据订单id查询订单信息,如果订单不存在, 直接返回错误
        Orders orders = this.getById(id);
        if (ObjectUtil.isNull(orders)) {
            throw new ForbiddenOperationException("订单不存在");
        }

        //2. 查询订单支付状态, 如果是已经支付 , 直接返回错误
        //transaction_id : 只有支付成功,才会有这个号
        if (orders.getPayStatus() == 4 && StringUtils.isNotEmpty(orders.getTransactionId())) {
            throw new ForbiddenOperationException("订单已经支付了");
        }

        //3. 调用支付微服务, 获取二维码
        NativePayReqDTO nativePayReqDTO = new NativePayReqDTO();
        nativePayReqDTO.setProductAppId("jzo2o.orders");//业务系统标识
        nativePayReqDTO.setProductOrderNo(id);//业务系统订单号
        nativePayReqDTO.setTradingChannel(ordersPayReqDTO.getTradingChannel());//支付渠道
        nativePayReqDTO.setTradingAmount(orders.getRealPayAmount());//支付金额
        nativePayReqDTO.setMemo(orders.getServeItemName());//备注

        //根据交易渠道设置商户号
        if (ObjectUtil.equal(ordersPayReqDTO.getTradingChannel(), PayChannelEnum.WECHAT_PAY)) {
            nativePayReqDTO.setEnterpriseId(tradeProperties.getWechatEnterpriseId());//微信商户号
        }
        if (ObjectUtil.equal(ordersPayReqDTO.getTradingChannel(), PayChannelEnum.ALI_PAY)) {
            nativePayReqDTO.setEnterpriseId(tradeProperties.getAliEnterpriseId());//阿里商户号
        }

        //原有的交易渠道不为空 而且跟刚刚传入交易渠道不一样
        if (StringUtils.isNotEmpty(orders.getTradingChannel()) &&
                !StringUtils.equals(orders.getTradingChannel(), ordersPayReqDTO.getTradingChannel().toString())
        ) {
            nativePayReqDTO.setChangeChannel(true);//是否改变交易渠道
        }else {
            nativePayReqDTO.setChangeChannel(false);//是否改变交易渠道
        }
        // 远程调用支付服务
        NativePayResDTO payResDTO = nativePayApi.createDownLineTrading(nativePayReqDTO);

        //4. 更新订单表数据(支付服务交易单号 支付渠道)
        orders.setTradingOrderNo(payResDTO.getTradingOrderNo());//支付服务交易单号
        orders.setTradingChannel(payResDTO.getTradingChannel());//支付渠道
        this.updateById(orders);

        //5. 封装返回结果
        OrdersPayResDTO ordersPayResDTO = BeanUtil.copyProperties(payResDTO, OrdersPayResDTO.class);
        ordersPayResDTO.setPayStatus(2);//支付状态: 未支付

        return ordersPayResDTO;
    }


    /**
     * 生成订单id
     *
     * @return 订单id 19位：2位年+2位月+2位日+13位序号(自增)
     */
    private Long generateOrderId() {
        //1. 2位年+2位月+2位日
        Long yyMMdd = DateUtils.getFormatDate(LocalDateTime.now(), "yyMMdd");

        //2. 自增数字  1 2
        Long num = redisTemplate.opsForValue().increment(RedisConstants.Lock.ORDERS_SHARD_KEY_ID_GENERATOR, 1);//1 代表的是每次增长量为1

        //3. 组装返回
        return yyMMdd * 10000000000000L + num;
    }
}
