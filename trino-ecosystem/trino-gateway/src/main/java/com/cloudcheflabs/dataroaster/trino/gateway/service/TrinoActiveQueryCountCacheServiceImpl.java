package com.cloudcheflabs.dataroaster.trino.gateway.service;

import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.CacheDao;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.CacheService;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.TrinoActiveQueryCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TrinoActiveQueryCountCacheServiceImpl implements CacheService<TrinoActiveQueryCount> {


    @Autowired
    @Qualifier("redisTrinoActiveQueryCountCacheDao")
    private CacheDao<TrinoActiveQueryCount> trinoActiveQueryCountCacheDao;


    @Override
    public void set(String id, TrinoActiveQueryCount trinoActiveQueryCount) {
        trinoActiveQueryCountCacheDao.set(id, trinoActiveQueryCount);
    }

    @Override
    public TrinoActiveQueryCount get(String id, Class<TrinoActiveQueryCount> clazz) {
        return trinoActiveQueryCountCacheDao.get(id, TrinoActiveQueryCount.class);
    }
}
