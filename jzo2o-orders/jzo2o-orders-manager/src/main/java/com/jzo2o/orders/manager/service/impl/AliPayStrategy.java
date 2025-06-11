package com.jzo2o.orders.manager.service.impl;

import com.jzo2o.orders.manager.service.PayStrategy;
import org.springframework.stereotype.Component;

/**
 * 阿里支付策略类
 */
@Component("aliPay")
public class AliPayStrategy implements PayStrategy {
    @Override
    public void pay() {
        System.out.println("使用支付宝支付");
    }
}
