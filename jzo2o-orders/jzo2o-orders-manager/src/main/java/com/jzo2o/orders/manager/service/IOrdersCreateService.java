package com.jzo2o.orders.manager.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.api.market.dto.request.CouponUseBackReqDTO;
import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.api.orders.dto.response.OrderResDTO;
import com.jzo2o.api.orders.dto.response.OrderSimpleResDTO;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.model.msg.TradeStatusMsg;
import com.jzo2o.orders.base.model.domain.Orders;
import com.jzo2o.orders.manager.model.dto.OrderCancelDTO;
import com.jzo2o.orders.manager.model.dto.request.OrderPageQueryReqDTO;
import com.jzo2o.orders.manager.model.dto.request.OrdersPayReqDTO;
import com.jzo2o.orders.manager.model.dto.request.PlaceOrderReqDTO;
import com.jzo2o.orders.manager.model.dto.response.OperationOrdersDetailResDTO;
import com.jzo2o.orders.manager.model.dto.response.OrdersPayResDTO;
import com.jzo2o.orders.manager.model.dto.response.PlaceOrderResDTO;

import java.util.List;

/**
 * <p>
 * 下单服务类
 * </p>
 *
 * @author itcast
 * @since 2023-07-10
 */
public interface IOrdersCreateService extends IService<Orders> {

    /**
     * 用户下单
     * @param placeOrderReqDTO
     * @return
     */
    PlaceOrderResDTO placeOrder(PlaceOrderReqDTO placeOrderReqDTO);

    /**
     * 用户下单
     * @param placeOrderReqDTO
     * @param userId
     * @return
     */
    PlaceOrderResDTO placeOrder(PlaceOrderReqDTO placeOrderReqDTO, Long userId);

    /**
     * 保存订单
     * @param orders
     */
    void saveOrder(Orders orders);

    /**
     * 订单支付
     * @param id
     * @param ordersPayReqDTO
     * @return
     */
    OrdersPayResDTO pay(Long id, OrdersPayReqDTO ordersPayReqDTO);

    /**
     * 查询订单支付结果
     * @param id
     * @return
     */
    OrdersPayResDTO getPayResultFromTradServer(Long id);

    /**
     * 获取可用优惠券
     * @param serveId
     * @param purNum
     * @return
     */
    List<AvailableCouponsResDTO> getAvailableCoupons(Long serveId, Integer purNum);
}
