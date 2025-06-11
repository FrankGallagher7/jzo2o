package com.jzo2o.orders.manager.service.impl;

import com.jzo2o.orders.manager.service.PayStrategy;
import org.springframework.stereotype.Component;

/**
 * 微信支付策略类
 */
@Component("wxPay")
public class WxPayStrategy implements PayStrategy {
    @Override
    public void pay() {
        System.out.println("微信支付");
    }
}
