package com.cloudcheflabs.dataroaster.apiserver.dao.ehcache;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.PrivilegesDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.Privileges;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;


@Repository
public class EhcachePrivilegesDao implements PrivilegesDao, InitializingBean {

    @Autowired
    private CacheManager cacheManager;

    private Cache cache;


    @Override
    public void afterPropertiesSet() throws Exception {
        cache = cacheManager.getCache("privileges");
    }

    @Override
    public void putPrivileges(String accessKey, Privileges privileges) {
        cache.put(accessKey, privileges);
    }

    @Override
    public Privileges getPrivileges(String accessToken) {
        return cache.get(accessToken, Privileges.class);
    }
}
