package com.jzo2o.foundations.handle;

import com.jzo2o.foundations.service.IRegionService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpringCacheSyncHandler {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IRegionService regionService;
    /**
     * 定时更新开通区域缓存
     */
    @XxlJob("activeRegionCacheSync")
    public void activeRegionCacheSync() {
        log.info("=============开始更新开通区域列表缓存============");
        //1. 使用redisTemplate删除当前缓存中开通区域列表
        redisTemplate.delete("JZ_CACHE::ACTIVE_REGIONS");

        //2. 重新将开通区域列表添加到缓存
        regionService.queryActiveRegionListCache();
    }
}
