package com.jzo2o.api.market;

import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(contextId = "jzo2o-market", value = "jzo2o-market", path = "/market/inner/coupon")
public interface CouponApi {
    /**
     * 获取可用优惠券列表（微服务调用）
     * @param totalAmount 订单总金额
     * @return 优惠券列表
     */
    @GetMapping("/getAvailable")
    public List<AvailableCouponsResDTO> getAvailable(@RequestParam("totalAmount") BigDecimal totalAmount);

}
