package com.jzo2o.foundations.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.RegionResDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;

import java.util.List;

/**
 * 区域管理
 *
 * @author itcast
 * @create 2023/7/17 16:49
 **/
public interface IServeService extends IService<Serve> {

    /**
     * 分页查询某个区域下目前所有的服务项目
     * @param servePageQueryReqDTO
     * @return
     */
    PageResult<ServeResDTO> pageQuery(ServePageQueryReqDTO servePageQueryReqDTO);

    /**
     * 某个区域下新增服务信息
     * @param serveDtoList
     */
    void addServe(List<ServeUpsertReqDTO> serveDtoList);

    /**
     * 删除区域服务
     * @param id
     */
    void deleteById(Long id);

    /**
     * 区域服务列表上架
     * @param id
     */
    void active(Long id);

    /**
     * 区域服务列表下架
     * @param id
     */
    void deactivate(Long id);
}
