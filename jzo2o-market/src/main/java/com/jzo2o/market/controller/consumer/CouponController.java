package com.jzo2o.market.controller.consumer;

import com.jzo2o.api.market.dto.request.CouponUseBackReqDTO;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.model.dto.PageQueryDTO;
import com.jzo2o.market.model.dto.request.SeizeCouponReqDTO;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.ICouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("consumerCouponController")
@RequestMapping("/consumer/coupon")
@Api(tags = "用户端-优惠券相关接口")
public class CouponController {

    @Autowired
    private ICouponService couponService;

    @ApiOperation("抢券")
    @PostMapping("/seize")
    public void seizeCoupon(@RequestBody SeizeCouponReqDTO seizeCouponReqDTO) {
        couponService.seizeCoupon(seizeCouponReqDTO);
    }

    @ApiOperation("用户端查询我的优惠券")
    @GetMapping("/my")
    public PageResult<CouponInfoResDTO> queryMyCoupon(PageQueryDTO pageQueryDTO, Long status) {
        return couponService.queryMyCoupon(status);
    }
}