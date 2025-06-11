package com.jzo2o.orders.manager.service;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 策略管理类
 */
@Component
public class PayStrategyManager {

    Map<String, PayStrategy> beansOfType = new HashMap<>();
    @PostConstruct
    public void init() {
        beansOfType = SpringUtil.getBeansOfType(PayStrategy.class);
    }

    // 根据用户需求, 执行指定的策略对象
    public void pay(String key) {
        PayStrategy payStrategy = beansOfType.get(key);
        payStrategy.pay();
    }
}
