package com.jzo2o.market.controller;


import com.jzo2o.market.model.dto.request.ActivitySaveReqDTO;
import com.jzo2o.market.service.IActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}