package com.jzo2o.orders.manager.service.impl;
import java.time.LocalDateTime;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.orders.dto.response.OrderResDTO;
import com.jzo2o.api.orders.dto.response.OrderSimpleResDTO;
import com.jzo2o.common.enums.EnableStatusEnum;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.utils.ObjectUtils;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.orders.base.enums.OrderPayStatusEnum;
import com.jzo2o.orders.base.enums.OrderRefundStatusEnum;
import com.jzo2o.orders.base.enums.OrderStatusEnum;
import com.jzo2o.orders.base.mapper.OrdersCanceledMapper;
import com.jzo2o.orders.base.mapper.OrdersMapper;
import com.jzo2o.orders.base.mapper.OrdersRefundMapper;
import com.jzo2o.orders.base.model.domain.Orders;
import com.jzo2o.orders.base.model.domain.OrdersCanceled;
import com.jzo2o.orders.base.model.domain.OrdersRefund;
import com.jzo2o.orders.base.model.dto.OrderSnapshotDTO;
import com.jzo2o.orders.base.model.dto.OrderUpdateStatusDTO;
import com.jzo2o.orders.base.service.IOrdersCommonService;
import com.jzo2o.orders.manager.model.dto.OrderCancelDTO;
import com.jzo2o.orders.manager.service.IOrdersManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.jzo2o.orders.base.constants.FieldConstants.SORT_BY;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-07-10
 */
@Slf4j
@Service
public class OrdersManagerServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersManagerService {


    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private IOrdersCommonService ordersCommonService;
    @Autowired
    private OrdersCanceledMapper ordersCanceledMapper;
    @Autowired
    private OrdersRefundMapper ordersRefundMapper;
    @Autowired
    private IOrdersManagerService owner;

    @Override
    public List<Orders> batchQuery(List<Long> ids) {
        LambdaQueryWrapper<Orders> queryWrapper = Wrappers.<Orders>lambdaQuery().in(Orders::getId, ids).ge(Orders::getUserId, 0);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Orders queryById(Long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 滚动分页查询
     *
     * @param currentUserId 当前用户id
     * @param ordersStatus  订单状态，0：待支付，100：派单中，200：待服务，300：服务中，400：待评价，500：订单完成，600：已取消，700：已关闭
     * @param sortBy        排序字段
     * @return 订单列表
     */
    @Override
    public List<OrderSimpleResDTO> consumerQueryList(Long currentUserId, Integer ordersStatus, Long sortBy) {
        //1.构件查询条件
        LambdaQueryWrapper<Orders> queryWrapper = Wrappers.<Orders>lambdaQuery()
                .eq(ObjectUtils.isNotNull(ordersStatus), Orders::getOrdersStatus, ordersStatus)
                .lt(ObjectUtils.isNotNull(sortBy), Orders::getSortBy, sortBy)
                .eq(Orders::getUserId, currentUserId)
                .eq(Orders::getDisplay, EnableStatusEnum.ENABLE.getStatus());
        Page<Orders> queryPage = new Page<>();
        queryPage.addOrder(OrderItem.desc(SORT_BY));
        queryPage.setSearchCount(false);

        //2.查询订单列表
        Page<Orders> ordersPage = baseMapper.selectPage(queryPage, queryWrapper);
        List<Orders> records = ordersPage.getRecords();
        List<OrderSimpleResDTO> orderSimpleResDTOS = BeanUtil.copyToList(records, OrderSimpleResDTO.class);
        return orderSimpleResDTOS;

    }
    /**
     * 根据订单id查询
     *
     * @param id 订单id
     * @return 订单详情
     */
    @Override
    public OrderResDTO getDetail(Long id) {
        Orders orders = queryById(id);
        OrderResDTO orderResDTO = BeanUtil.toBean(orders, OrderResDTO.class);
        return orderResDTO;
    }

    /**
     * 订单评价
     *
     * @param ordersId 订单id
     */
    @Override
    @Transactional
    public void evaluationOrder(Long ordersId) {
//        //查询订单详情
//        Orders orders = queryById(ordersId);
//
//        //构建订单快照
//        OrderSnapshotDTO orderSnapshotDTO = OrderSnapshotDTO.builder()
//                .evaluationTime(LocalDateTime.now())
//                .build();
//
//        //订单状态变更
//        orderStateMachine.changeStatus(orders.getUserId(), orders.getId().toString(), OrderStatusChangeEventEnum.EVALUATE, orderSnapshotDTO);
    }

    /**
     * 取消订单
     * @param orderCancelDTO
     */
    @Override
    public void cancel(OrderCancelDTO orderCancelDTO) {
        // 判断有没有这个订单
        Orders orders = ordersMapper.selectById(orderCancelDTO.getId());
        if (ObjectUtil.isNull(orders)) {
            throw new RuntimeException("订单不存在");
        }
        // 赋值OrderCancelDTO
        BeanUtil.copyProperties(orders,  orderCancelDTO);
        // 判断订单状态0：待支付，100：派单中，200：待服务，300：服务中，500：订单完成，600：已取消，700：已关闭'、
        OrderUpdateStatusDTO orderUpdateStatusDTO = BeanUtil.copyProperties(orders, OrderUpdateStatusDTO.class);

        // 1.修改订单记录
        if (ObjectUtil.equal(orders.getOrdersStatus(),OrderStatusEnum.NO_PAY.getStatus())) {

            // 修改订单状态--取消待支付订单--如果为待支付，修改订单状态，插入订单取消表
            owner.updateNoPay(orderUpdateStatusDTO);

        } else if (ObjectUtil.equal(orders.getOrdersStatus(),OrderStatusEnum.DISPATCHING.getStatus())){
            // 修改订单状态--取消已支付订单--如果为派单中，修改订单状态，插入订单取消表，插入退款表记录
            owner.updateDispatching(orderUpdateStatusDTO,orderCancelDTO);
        } else {
            throw new RuntimeException("该订单状态无法取消");
        }
        // 2.保存取消订单记录
        OrdersCanceled ordersCanceled = new OrdersCanceled();
        ordersCanceled.setId(orderCancelDTO.getId());//订单id
        ordersCanceled.setCancellerId(orderCancelDTO.getCurrentUserId());//取消人
        ordersCanceled.setCancelerName(orderCancelDTO.getCurrentUserName());//取消人名称
        ordersCanceled.setCancellerType(orderCancelDTO.getCurrentUserType());//取消人类型，1：普通用户，4：运营人员
        ordersCanceled.setCancelReason(orderCancelDTO.getCancelReason());//取消原因
        ordersCanceled.setCancelTime(LocalDateTime.now());//取消时间
        ordersCanceledMapper.insert(ordersCanceled);


    }


    // 修改待支付订单记录-0
    @Transactional(rollbackFor = Exception.class)
    public void updateNoPay(OrderUpdateStatusDTO orderUpdateStatusDTO) {
        //原订单状态
        orderUpdateStatusDTO.setOriginStatus(OrderStatusEnum.NO_PAY.getStatus());
        //目标订单状态
        orderUpdateStatusDTO.setTargetStatus(OrderStatusEnum.CANCELED.getStatus());
        Integer i = ordersCommonService.updateStatus(orderUpdateStatusDTO);
        if (i <= 0) {
            throw new ForbiddenOperationException("订单取消失败");
        }
    }
    // 修改派单中订单记录-100
    @Transactional(rollbackFor = Exception.class)
    public void updateDispatching(OrderUpdateStatusDTO orderUpdateStatusDTO,OrderCancelDTO orderCancelDTO) {
        //原订单状态
        orderUpdateStatusDTO.setOriginStatus(OrderStatusEnum.DISPATCHING.getStatus());
        //目标订单状态
        orderUpdateStatusDTO.setTargetStatus(OrderStatusEnum.CLOSED.getStatus());
        //退款状态
        orderUpdateStatusDTO.setRefundStatus(OrderRefundStatusEnum.REFUNDING.getStatus());
        Integer i = ordersCommonService.updateStatus(orderUpdateStatusDTO);
        if (i <= 0) {
            throw new ForbiddenOperationException("订单取消失败");
        }

        // 保存退款记录
        OrdersRefund ordersRefund = new OrdersRefund();
        ordersRefund.setId(orderUpdateStatusDTO.getId());//订单id
        ordersRefund.setTradingOrderNo(orderUpdateStatusDTO.getTradingOrderNo());//支付服务交易单号
        ordersRefund.setRealPayAmount(orderCancelDTO.getRealPayAmount());//实付金额
        ordersRefund.setCreateTime(LocalDateTime.now());//创建时间

        ordersRefundMapper.insert(ordersRefund);
    }

}
