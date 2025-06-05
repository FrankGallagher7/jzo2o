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
import com.jzo2o.foundations.model.dto.response.ServeCategoryResDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.model.dto.response.ServeSimpleResDTO;
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

    /**
     * 区域服务列表下架
     * @param id
     */
    @Override
    public void deactivate(Long id) {
        // 区域服务当前为上架状态
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isEmpty(serve) || serve.getSaleStatus() != 2) {
            throw new ForbiddenOperationException("当前区域服务是下架状态");
        }

        // 更新服务状态为下架状态
        serve.setSaleStatus(1);
        baseMapper.updateById(serve);
    }

    /**
     * 设置区域服务为热门
     *
     * @param id
     */
    @Override
    public void onHot(Long id) {
        // 获取当前时间戳
        long timeStamp = System.currentTimeMillis();
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isEmpty(serve)) {
            throw new ForbiddenOperationException("当前服务不存在");
        }
        serve.setIsHot(1);
        serve.setHotTimeStamp(timeStamp);
        // 更新服务状态为热门
        baseMapper.updateById(serve);
    }

    /**
     * 设置区域服务为非热门
     * @param id
     */
    @Override
    public void offHot(Long id) {
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isEmpty(serve)) {
            throw new ForbiddenOperationException("当前服务不存在");
        }
        // 删除服务状态为热门
        serve.setIsHot(0);
        serve.setHotTimeStamp(null);
        baseMapper.updateById(serve);
    }

    /**
     * 根据区域id查询服务列表
     * @param regionId
     * @return
     */
    @Override
    public List<ServeCategoryResDTO> findServeByRegionId(Long regionId) {
        // 1.判断是否有该地区，或者该区是否开放
        Region region = regionMapper.selectById(regionId);
        if (ObjectUtil.isNull(region) || region.getActiveStatus() != 2) {
            return List.of();
        }
        // 2.获取地区服务列表
        List<ServeCategoryResDTO> list = baseMapper.findServeByRegionId(regionId);
        if (list.isEmpty()) {
            return List.of();
        }
        // 3.切割（区域前两个，每个区域项目4个）
        // 区域
        List<ServeCategoryResDTO> serveTypeList = list.subList(0, Math.min(list.size(), 2));
        // 服务
        serveTypeList.forEach(s -> {
            List<ServeSimpleResDTO> serveSimpleResDTOS = s.getServeResDTOList().subList(0, Math.min(s.getServeResDTOList().size(), 4));
            s.setServeResDTOList(serveSimpleResDTOS);
        });
        return serveTypeList;
    }
}
