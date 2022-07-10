package com.cloudcheflabs.dataroaster.trino.gateway.dao.redis;

import com.cloudcheflabs.dataroaster.trino.gateway.dao.common.AbstractCacheDao;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.TrinoResponse;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class RedisTrinoResponseCacheDao extends AbstractCacheDao<TrinoResponse> {

    public RedisTrinoResponseCacheDao() {
        super(TrinoResponse.class);
    }
}
