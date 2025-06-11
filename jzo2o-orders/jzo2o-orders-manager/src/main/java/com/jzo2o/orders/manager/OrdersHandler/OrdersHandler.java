package com.jzo2o.orders.manager.OrdersHandler;

import cn.hutool.core.collection.CollUtil;
import com.jzo2o.api.trade.RefundRecordApi;
import com.jzo2o.api.trade.dto.response.ExecutionResultResDTO;
import com.jzo2o.orders.base.enums.OrderRefundStatusEnum;
import com.jzo2o.orders.base.model.domain.Orders;
import com.jzo2o.orders.base.model.domain.OrdersRefund;
import com.jzo2o.orders.manager.service.IOrdersManagerService;
import com.jzo2o.orders.manager.service.IOrdersRefundService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单处理器类
 */
@Component
@Slf4j
public class OrdersHandler {

    @Autowired
    private IOrdersRefundService ordersRefundService;

    @Autowired
    private RefundRecordApi refundRecordApi;

    @Autowired
    private IOrdersManagerService ordersManagerService;

    @Autowired
    private OrdersHandler owner;

    /**
     * 定时读取退款表中的数据, 然后调用支付服务的退款接口
     */
    @XxlJob(value = "handleRefundOrders")
    public void handleRefundOrders() {
        //1. 读取退款表中的数据
        List<OrdersRefund> ordersRefundList = ordersRefundService.queryRefundOrderListByCount(100);
        if (CollUtil.isEmpty(ordersRefundList)){
            return;
        }

        //2. 遍历查询到的数据
        for (OrdersRefund ordersRefund : ordersRefundList) {
            //3. 然后调用支付服务的退款接口
            ExecutionResultResDTO executionResultResDTO
                    = refundRecordApi.refundTrading(ordersRefund.getTradingOrderNo(), ordersRefund.getRealPayAmount());

            if (executionResultResDTO != null){
                //4. 根据退款接口的返回值做处理
                if (executionResultResDTO.getRefundStatus() == OrderRefundStatusEnum.REFUNDING.getStatus()) {
                    continue;//如果返回值是退款中, 不做后续处理
                }

                //退款后续操作
                owner.afterRefund(ordersRefund,executionResultResDTO);
            }
        }
    }

    @Transactional
    public void afterRefund(OrdersRefund ordersRefund,ExecutionResultResDTO executionResultResDTO) {
        //1) 更新订单表中退款相关字段(refund_status 退款状态 refund_no 支付服务退款单号 refund_id 第三方支付的退款单号)
        Orders orders = new Orders();
        orders.setId(ordersRefund.getId());
        orders.setRefundNo(executionResultResDTO.getRefundNo());
        orders.setRefundId(executionResultResDTO.getRefundId());
        orders.setRefundStatus(executionResultResDTO.getRefundStatus());
        boolean b = ordersManagerService.updateById(orders);

        //2) 删除退款表中的数据
        if (b){
            ordersRefundService.removeById(ordersRefund.getId());
        }
    }
}