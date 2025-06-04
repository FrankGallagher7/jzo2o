package com.jzo2o.foundations.controller.operation;


import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.model.dto.request.RegionPageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.RegionUpsertReqDTO;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.RegionResDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 区域服务表 前端控制器
 * </p>
 *
 * @author itcast
 * @since 2023-07-03
 */
@RestController("serveController")
@RequestMapping("/operation/serve")
@Api(tags = "运营端 - 区域服务相关接口1")
public class ServeController {

    @Autowired
    private IServeService serveService;

    /**
     * 本接口用于分页查询某个区域下目前所有的服务项目
     *
     * 接口路径：GET    /foundations/operation/serve/page
     * @param servePageQueryReqDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("区域服务分页查询")
    public PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO) {
        return serveService.pageQuery(servePageQueryReqDTO);
    }

    /**
     * 某个区域下新增服务信息，也就是向serve表添加指定区域的对应的服务信息，且一次性可以保存多条记录，注意：
     * 1. 服务项目必须是启用状态的才能添加到区域
     * 2. 一个服务项目对于一个区域，只能添加一次
     *
     * 接口路径：POST  /foundations/operation/serve/batch
     */
    @PostMapping("/batch")
    @ApiOperation("区域服务新增")
    public void add(@RequestBody List<ServeUpsertReqDTO> serveDtoList) {
        serveService.addServe(serveDtoList);
    }

}
