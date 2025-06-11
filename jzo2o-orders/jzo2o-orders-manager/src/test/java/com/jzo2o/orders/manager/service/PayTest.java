package com.jzo2o.orders.manager.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 测试支付方式的选择
 */
@SpringBootTest
public class PayTest {

    @Autowired
    private PayStrategyManager payStrategyManager;

    @Test
    public void test(){
        //测试微信支付
        payStrategyManager.pay("wxPay");
        //测试阿里支付
        payStrategyManager.pay("aliPay");
    }
}