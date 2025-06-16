package com.jzo2o.market.controller.inner;

import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.market.service.ICouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController("innerCouponController")
@RequestMapping("/inner/coupon")
@Api(tags = "内部接口-优惠券相关接口")
public class CouponController{

    @Autowired
    private ICouponService couponService;

    /**
     * 获取可用优惠券列表（微服务调用）
     * @param totalAmount
     * @return
     */
    @ApiOperation("获取可用优惠券列表")
    @GetMapping("/getAvailable")
    public List<AvailableCouponsResDTO> getAvailable(@RequestParam("totalAmount") BigDecimal totalAmount) {
        return couponService.getAvailable(totalAmount);
    }
}