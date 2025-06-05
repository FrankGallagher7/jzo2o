package com.jzo2o.foundations.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jzo2o.api.foundations.dto.response.ServeAggregationResDTO;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.dto.response.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 * @since 2023-07-03
 */
public interface ServeMapper extends BaseMapper<Serve> {
    /**
     * 分页查询某个区域下目前所有的服务项目
     * @param regionId
     * @return
     */
    List<ServeResDTO> pageQuery(Long regionId);

    /**
     * 根据地区id查询某个区域下目前所有服务项目
     * @param regionId
     * @return
     */
    List<ServeCategoryResDTO> findServeByRegionId(Long regionId);

    /**
     * 根据地区id查询热点服务
     * @param regionId
     * @return
     */
    List<ServeAggregationSimpleResDTO> findHotServeByRegionId(Long regionId);

    /**
     * 根据区域id查询全部服务
     * @param regionId
     * @return
     */
    List<ServeAggregationTypeSimpleResDTO> findAllServeTypeList(Long regionId);
}
