package com.jzo2o.orders.manager.strategy.impl;

import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.orders.base.enums.OrderStatusEnum;
import com.jzo2o.orders.base.mapper.OrdersCanceledMapper;
import com.jzo2o.orders.base.mapper.OrdersMapper;
import com.jzo2o.orders.base.mapper.OrdersRefundMapper;
import com.jzo2o.orders.base.model.domain.OrdersCanceled;
import com.jzo2o.orders.base.model.dto.OrderUpdateStatusDTO;
import com.jzo2o.orders.base.service.IOrdersCommonService;
import com.jzo2o.orders.manager.model.dto.OrderCancelDTO;
import com.jzo2o.orders.manager.service.IOrdersManagerService;
import com.jzo2o.orders.manager.strategy.OrderCancelStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

//普通用户取消未支付订单--0
@Component("1:NO_PAY")//用户类型:订单状态
public class CommonUserNoPayOrderCancelStrategy implements OrderCancelStrategy {
    @Autowired
    private IOrdersCommonService ordersCommonService;
    @Autowired
    private OrdersCanceledMapper ordersCanceledMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(OrderCancelDTO orderCancelDTO) {
        // 1.更新订单状态
        OrderUpdateStatusDTO orderUpdateStatusDTO = new OrderUpdateStatusDTO();
        orderUpdateStatusDTO.setId(orderCancelDTO.getId()); // 订单id
        orderUpdateStatusDTO.setOriginStatus(OrderStatusEnum.NO_PAY.getStatus()); //原订单状态
        orderUpdateStatusDTO.setTargetStatus(OrderStatusEnum.CANCELED.getStatus()); //目标订单状态
        Integer i = ordersCommonService.updateStatus(orderUpdateStatusDTO);
        if (i <= 0) {
            throw new ForbiddenOperationException("订单取消失败");
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
}
