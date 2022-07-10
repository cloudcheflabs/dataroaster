package com.cloudcheflabs.dataroaster.trino.gateway.cache;

import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.CacheDao;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.CacheService;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.TrinoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component
public class TrinoResponseRedisCache implements CacheService<TrinoResponse> {

    @Autowired
    @Qualifier("redisTrinoResponseCacheDao")
    private CacheDao<TrinoResponse> trinoResponseCacheDao;


    @Override
    public void set(String id, TrinoResponse trinoResponse) {
        trinoResponseCacheDao.set(id, trinoResponse);
    }

    @Override
    public TrinoResponse get(String id, Class<TrinoResponse> clazz) {
        return trinoResponseCacheDao.get(id, clazz);
    }
}
