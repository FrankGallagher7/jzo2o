package com.jzo2o.foundations.handle;

import com.jzo2o.api.foundations.dto.response.RegionSimpleResDTO;
import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.service.IRegionService;
import com.jzo2o.foundations.service.IServeService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SpringCacheSyncHandler {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IRegionService regionService;
    @Autowired
    private IServeService serveService;

    @XxlJob("activeRegionCacheSync")
    public void activeRegionCacheSync() {
        /**
         * 定时更新开通区域缓存
         */
        log.info("=============开始更新开通区域列表缓存============");
        //1. 使用redisTemplate删除当前缓存中开通区域列表
        redisTemplate.delete("JZ_CACHE::ACTIVE_REGIONS");

        //2. 重新将开通区域列表添加到缓存
        List<RegionSimpleResDTO> regionDtoList =  regionService.queryActiveRegionListCache();
        /**
         * 定时更新首页服务列表缓存
         */
        regionDtoList.forEach(regionDto -> {

            log.info("=============开始更新首页服务列表缓存============");
            // 清除缓存
            redisTemplate.delete(RedisConstants.CacheName.SERVE_ICON + "::" + regionDto.getId());
            // 添加缓存
            serveService.findServeByRegionId(regionDto.getId());
        });

    }
}
