package com.jzo2o.foundations.controller.operation;


import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.dto.request.RegionPageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.RegionUpsertReqDTO;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.RegionResDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
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

    /**
     * 点击区域价格进行修改，本质就是根据区域服务的id对price字段进行修改
     * 接口路径：PUT  /foundations/operation/serve/{id}
     * @param id
     * @param price
     */
    @PutMapping("/{id}")
    @ApiOperation("区域服务修改价格")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "区域服务id", required = true, dataTypeClass = Long.class),
            @ApiImplicitParam(name = "price", value = "价格", required = true, dataTypeClass = BigDecimal.class),
    })
    public void update(@PathVariable("id") Long id, BigDecimal price) {
        Serve serve = new Serve();
        serve.setId(id);
        serve.setPrice(price);
        serveService.updateById(serve);
    }

    /**
     * 删除区域服务
     * @param id
     */
    @DeleteMapping("/{id}")
    @ApiOperation("区域服务删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "区域服务id", required = true, dataTypeClass = Long.class)
    })
    public void delete(@PathVariable("id") Long id) {
        serveService.deleteById(id);
    }

    /**
     * 上架
     * 在区域服务列表上架，此服务在该区域将生效（本质就是要修改区域服务的状态为2）
     * 上架成功的必要条件有两个：1) 区域服务当前非上架状态   2) 服务项目是启用状态
     * 接口路径：PUT  /foundations/operation/serve/onSale/{id}
     * @param id
     */
    @PutMapping("/onSale/{id}")
    @ApiOperation("区域服务上架")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "区域服务id", required = true, dataTypeClass = Long.class),
    })
    public void activate(@PathVariable("id") Long id) {
        serveService.active(id);
    }


    /**
     * 在区域服务列表下架（本质就是要修改区域服务的状态为1）,下架成功的必要条件是区域服务当前为上架状态
     * @param id
     */
    @PutMapping("/offSale/{id}")
    @ApiOperation("区域服务下架")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "区域服务id", required = true, dataTypeClass = Long.class),
    })
    public void deactivate(@PathVariable("id") Long id) {
        serveService.deactivate(id);
    }


}
