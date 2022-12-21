package com.cloudcheflabs.dataroaster.trino.gateway.dao.redis;

import com.cloudcheflabs.dataroaster.trino.gateway.dao.common.AbstractCacheDao;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.TrinoActiveQueryCount;
import org.springframework.stereotype.Repository;

@Repository
public class RedisTrinoActiveQueryCountCacheDao extends AbstractCacheDao<TrinoActiveQueryCount> {

    public RedisTrinoActiveQueryCountCacheDao() {
        super(TrinoActiveQueryCount.class);
    }
}
