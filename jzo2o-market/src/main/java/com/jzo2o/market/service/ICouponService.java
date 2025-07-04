package com.jzo2o.market.service;

import com.jzo2o.api.market.dto.request.CouponUseBackReqDTO;
import com.jzo2o.api.market.dto.request.CouponUseReqDTO;
import com.jzo2o.api.market.dto.response.AvailableCouponsResDTO;
import com.jzo2o.api.market.dto.response.CouponUseResDTO;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.market.model.domain.Coupon;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.market.model.dto.request.CouponOperationPageQueryReqDTO;
import com.jzo2o.market.model.dto.request.SeizeCouponReqDTO;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
public interface ICouponService extends IService<Coupon> {


    /**
     * 查询优惠券领取记录
     * @param dto
     * @return
     */
    PageResult<CouponInfoResDTO> findByPage(CouponOperationPageQueryReqDTO dto);

    /**
     * 已领取优惠券自动过期任务
     */
    void processExpireCoupon();

    /**
     *抢券
     * @param seizeCouponReqDTO
     */
    void seizeCoupon(SeizeCouponReqDTO seizeCouponReqDTO);



    /**
     * 用户端查询我的优惠券列表
     * @param lastId
     * @param userId
     * @param status
     * @return
     */
    List<CouponInfoResDTO> queryForList(Long lastId, Long userId, Integer status);

    /**
     * 获取可用优惠券列表（微服务调用）
     * @param totalAmount
     * @return
     */
    List<AvailableCouponsResDTO> getAvailable(BigDecimal totalAmount);

    /**
     * 核销优惠券
     * @param couponUseReqDTO
     * @return
     */
    CouponUseResDTO use(CouponUseReqDTO couponUseReqDTO);

    /**
     * 回退优惠券
     * @param couponUseBackReqDTO
     */
    void useBack(CouponUseBackReqDTO couponUseBackReqDTO);
}
