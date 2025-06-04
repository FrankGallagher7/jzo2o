package com.jzo2o.foundations.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.mapper.RegionMapper;
import com.jzo2o.foundations.mapper.ServeItemMapper;
import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.model.domain.Region;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.RegionResDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import com.jzo2o.mysql.utils.PageHelperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 区域管理
 *
 * @author itcast
 * @create 2023/7/17 16:50
 **/
@Service

public class ServeServiceImpl extends ServiceImpl<ServeMapper, Serve> implements IServeService {

    @Autowired
    private ServeItemMapper serveItemMapper;

    @Autowired
    private RegionMapper regionMapper;

    /**
     * 分页查询某个区域下目前所有的服务项目
     * @param servePageQueryReqDTO
     * @return
     */
    @Override
    public PageResult<ServeResDTO> pageQuery(ServePageQueryReqDTO servePageQueryReqDTO) {
        return PageHelperUtils.selectPage(servePageQueryReqDTO,
                () -> baseMapper.pageQuery(servePageQueryReqDTO.getRegionId()));
    }

    /**
     * 某个区域下新增服务信息
     * @param serveDtoList
     */
    @Override
    public void addServe(List<ServeUpsertReqDTO> serveDtoList) {
        for (ServeUpsertReqDTO serveUpsertReqDTO : serveDtoList) {
            // 1. 服务项目必须是启用状态的才能添加到区域
            ServeItem serveItem = serveItemMapper.selectById(serveUpsertReqDTO.getServeItemId());
            if (ObjectUtil.isEmpty(serveItem) || serveItem.getActiveStatus() != 2) {
                throw new ForbiddenOperationException("服务项不存在或未启用");
            }

            // 2. 一个服务项目对于一个区域，只能添加一次
            Integer count = baseMapper.selectCount(new LambdaQueryWrapper<Serve>()
                    .eq(Serve::getServeItemId, serveUpsertReqDTO.getServeItemId())
                    .eq(Serve::getRegionId, serveUpsertReqDTO.getRegionId()));
            if (count > 0) {
                throw new ForbiddenOperationException("服务项已经添加到该区域");
            }

            // 3. 新增服务信息
            // TODO 后续处理性能优化
            Serve serve = BeanUtil.copyProperties(serveUpsertReqDTO, Serve.class);
            // 处理城市编码
            Region region = regionMapper.selectById(serveUpsertReqDTO.getRegionId());
            if (ObjectUtil.isNotEmpty(region)) {
                serve.setCityCode(region.getCityCode());
            }
            baseMapper.insert(serve);

        }

    }

    /**
     * 删除区域服务
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        // 要求当状态为草稿状态方可删除
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isNull(serve) || serve.getSaleStatus() != 0){
            throw new ForbiddenOperationException("删除失败, 当前区域服务不是草稿状态");
        }
        //根据主键删除
        baseMapper.deleteById(id);
    }

    /**
     * 区域服务列表上架
     * @param id
     */
    @Override
    public void active(Long id) {
        // 1) 区域服务当前非上架状态
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isEmpty(serve) || serve.getSaleStatus() == 2) {
            throw new ForbiddenOperationException("当前区域服务是上架状态");
        }
        // 2) 服务项目是启用状态
        ServeItem serveItem = serveItemMapper.selectById(serve.getServeItemId());
        if (ObjectUtil.isEmpty(serveItem) || serveItem.getActiveStatus() != 2) {
            throw new ForbiddenOperationException("服务项不存在或未启用");
        }
        // 3) 更新服务状态为上架状态
        serve.setSaleStatus(2);
        baseMapper.updateById(serve);
    }
}
