package com.jzo2o.foundations.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.mapper.*;
import com.jzo2o.foundations.model.domain.*;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.*;
import com.jzo2o.foundations.service.IServeService;
import com.jzo2o.mysql.utils.PageHelperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        //4）添加同步表数据
        addServeSync(id);
    }
    @Autowired
    private ServeTypeMapper serveTypeMapper;

    @Autowired
    private ServeSyncMapper serveSyncMapper;


    /**
     * 新增服务同步数据
     *
     * @param serveId 服务id
     */
    private void addServeSync(Long serveId) {
        //服务信息
        Serve serve = baseMapper.selectById(serveId);
        //区域信息
        Region region = regionMapper.selectById(serve.getRegionId());
        //服务项信息
        ServeItem serveItem = serveItemMapper.selectById(serve.getServeItemId());
        //服务类型
        ServeType serveType = serveTypeMapper.selectById(serveItem.getServeTypeId());

        ServeSync serveSync = new ServeSync();
        serveSync.setServeTypeId(serveType.getId());
        serveSync.setServeTypeName(serveType.getName());
        serveSync.setServeTypeIcon(serveType.getServeTypeIcon());
        serveSync.setServeTypeImg(serveType.getImg());
        serveSync.setServeTypeSortNum(serveType.getSortNum());

        serveSync.setServeItemId(serveItem.getId());
        serveSync.setServeItemIcon(serveItem.getServeItemIcon());
        serveSync.setServeItemName(serveItem.getName());
        serveSync.setServeItemImg(serveItem.getImg());
        serveSync.setServeItemSortNum(serveItem.getSortNum());
        serveSync.setUnit(serveItem.getUnit());
        serveSync.setDetailImg(serveItem.getDetailImg());
        serveSync.setPrice(serve.getPrice());

        serveSync.setCityCode(region.getCityCode());
        serveSync.setId(serve.getId());
        serveSync.setIsHot(serve.getIsHot());
        serveSyncMapper.insert(serveSync);
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
        // 删除同步表数据
        serveSyncMapper.deleteById(id);
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

    @Caching(
            cacheable = {
                    //返回数据为空，则缓存空值30分钟，这样可以避免缓存穿透
                    @Cacheable(value = RedisConstants.CacheName.SERVE_ICON, key ="#regionId" ,
                            unless ="#result.size() > 0",cacheManager = RedisConstants.CacheManager.THIRTY_MINUTES),

                    //返回值不为空，则永久缓存数据
                    @Cacheable(value = RedisConstants.CacheName.SERVE_ICON, key ="#regionId" ,
                            unless ="#result.size() == 0",cacheManager = RedisConstants.CacheManager.FOREVER)
            }
    )
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
//        List<ServeCategoryResDTO> serveTypeList = list.subList(0, Math.min(list.size(), 2));
        List<ServeCategoryResDTO> serveTypeList = new ArrayList<>(
                list.subList(0, Math.min(list.size(), 2))
        );
        // 服务
        serveTypeList.forEach(s -> {
            List<ServeSimpleResDTO> serveSimpleResDTOS = s.getServeResDTOList().subList(0, Math.min(s.getServeResDTOList().size(), 4));
            // 创建新的ArrayList
            s.setServeResDTOList(new ArrayList<>(serveSimpleResDTOS));
        });
        return serveTypeList;
    }

    /**
     * 根据地区id查询热点服务
     * @param regionId
     * @return
     */
    @Cacheable(value = RedisConstants.CacheName.HOT_SERVE, key ="#regionId" ,cacheManager = RedisConstants.CacheManager.FOREVER)
    @Override
    public List<ServeAggregationSimpleResDTO> findHotServeByRegionId(Long regionId) {
        // 1.查询地区
        Region region = regionMapper.selectById(regionId);
        if (ObjectUtil.isNull(region) || region.getActiveStatus() != 2) {
            throw new ForbiddenOperationException("当前地区不存在或未开放");
        }
        // 2.查询热点服务
        List<ServeAggregationSimpleResDTO> list = baseMapper.findHotServeByRegionId(regionId);
        return list;
    }

    /**
     * 根据服务id查询服务详情
     * @param id
     * @return
     */
    @Override
    public ServeAggregationSimpleResDTO findById(Long id) {
        // 1.查询是否有该服务
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isNull(serve)) {
            throw new ForbiddenOperationException("当前服务不存在");
        }
        // 2.查询服务详情
        // 查询服务项目信息
        ServeItem serveItem = serveItemMapper.selectById(serve.getServeItemId());
        if (ObjectUtil.isNull(serveItem)) {
            throw new ForbiddenOperationException("当前服务项不存在");
        }
        // 3.封装
        ServeAggregationSimpleResDTO serveAggregationSimpleResDTO = BeanUtil.copyProperties(serve, ServeAggregationSimpleResDTO.class);
        serveAggregationSimpleResDTO.setServeItemName(serveItem.getName());
        serveAggregationSimpleResDTO.setServeItemImg(serveItem.getImg());
        serveAggregationSimpleResDTO.setDetailImg(serveItem.getDetailImg());
        serveAggregationSimpleResDTO.setUnit(serveItem.getUnit());
        return serveAggregationSimpleResDTO;
    }

    /**
     * 根据区域id查询全部服务
     * @param regionId
     * @return
     */
    @Override
    public List<ServeAggregationTypeSimpleResDTO> findAllServeTypeList(Long regionId) {
        // 1.查询是否有该地区
        Region region = regionMapper.selectById(regionId);
        if (ObjectUtil.isNull(region)) {
            return List.of();
        }
        // 2.根据区域id查询服务类型列表
        return baseMapper.findAllServeTypeList(regionId);
    }
}
