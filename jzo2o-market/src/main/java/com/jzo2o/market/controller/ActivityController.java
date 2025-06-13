package com.jzo2o.market.controller;


import com.jzo2o.common.model.PageResult;
import com.jzo2o.market.model.dto.request.ActivityQueryForPageReqDTO;
import com.jzo2o.market.model.dto.request.ActivitySaveReqDTO;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("operationActivityController")
@RequestMapping("/operation/activity")
@Api(tags = "运营端 - 优惠券活动相关接口")
public class ActivityController {

    @Autowired
    private IActivityService activityService;

    /**
     * 新增或修改一个优惠券活动
     * @param dto
     */
    @ApiOperation("新增或修改一个优惠券活动")
    @PostMapping("/save")
    public void saveOrUpdate(@RequestBody ActivitySaveReqDTO dto) {
        activityService.saveOrUpdateActivity(dto);
    }

    /**
     * 运营端分页查询活动
     * @param dto
     * @return
     */
    @ApiOperation("运营端分页查询活动")
    @GetMapping("/page")
    public PageResult<ActivityInfoResDTO> findByPage(ActivityQueryForPageReqDTO dto) {
        return activityService.findByPage(dto);
    }

    /**
     * 查询优惠券活动详情
     * @param id
     * @return
     */
    @ApiOperation("查询活动详情")
    @GetMapping("/{id}")
    public ActivityInfoResDTO getDetail(@PathVariable("id") Long id) {
        return activityService.findById(id);
    }

    /**
     * 撤销活动
     * @param id
     */
    @ApiOperation("活动撤销")
    @PostMapping("/revoke/{id}")
    public void revoke(@PathVariable("id") Long id) {
        activityService.revoke(id);
    }
}