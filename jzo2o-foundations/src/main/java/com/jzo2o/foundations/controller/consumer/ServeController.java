package com.jzo2o.foundations.controller.consumer;


import com.jzo2o.foundations.model.dto.response.ServeAggregationSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeCategoryResDTO;
import com.jzo2o.foundations.service.IServeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 小程序服务 前端控制器
 * </p>
 *
 * @author itcast
 * @since 2023-07-03
 */
@RestController("consumerServeController")
@RequestMapping("customer/serve")
@Api(tags = "用户端 - 区域相关接口")
public class ServeController {

    @Autowired
    private IServeService serveService;

    /**
     * 根据区域id查询服务列表
     * @param regionId
     * @return
     */
    @ApiOperation("根据区域id查询服务列表")
    @GetMapping("/firstPageServeList")
    public List<ServeCategoryResDTO> findServeByRegionId(Long regionId) {
        List<ServeCategoryResDTO> result = serveService.findServeByRegionId(regionId);
        return result;
    }

    /**
     * 根据地区id查询热点服务
     * @return
     */
    @ApiOperation("根据地区id查询热点服务")
    @GetMapping("/hotServeList")
    public List<ServeAggregationSimpleResDTO> findHotServeByRegionId(Long regionId) {
        List<ServeAggregationSimpleResDTO> result = serveService.findHotServeByRegionId(regionId);
        return result;
    }
}
