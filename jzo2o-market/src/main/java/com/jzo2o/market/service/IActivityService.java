package com.jzo2o.market.service;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.market.model.domain.Activity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.market.model.dto.request.ActivityQueryForPageReqDTO;
import com.jzo2o.market.model.dto.request.ActivitySaveReqDTO;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;
import com.jzo2o.market.model.dto.response.SeizeCouponInfoResDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
public interface IActivityService extends IService<Activity> {


    /**
     * 新增或修改一个优惠券活动
     * @param dto
     */
    void saveOrUpdateActivity(ActivitySaveReqDTO dto);

    /**
     * 运营端分页查询活动
     *
     * @param dto 查询条件
     * @return 优惠券活动分页结果
     */
    PageResult<ActivityInfoResDTO> findByPage(ActivityQueryForPageReqDTO dto);

    /**
     * 查询优惠券活动详情
     * @param id
     * @return
     */
    ActivityInfoResDTO findById(Long id);

    /**
     * 撤销活动
     * @param id
     */
    void revoke(Long id);
}
