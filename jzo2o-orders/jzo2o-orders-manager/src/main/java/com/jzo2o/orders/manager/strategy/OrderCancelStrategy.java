package com.jzo2o.orders.manager.strategy;

import com.jzo2o.orders.manager.model.dto.OrderCancelDTO;

/**
 * 订单取消策略接口
 */
public interface OrderCancelStrategy {
    /**
     * 取消订单
     * @param orderCancelDTO
     */
    void cancel(OrderCancelDTO orderCancelDTO);

}
