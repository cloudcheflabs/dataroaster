package com.cloudcheflabs.dataroaster.trino.gateway.service;

import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.ClusterDao;
import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.ClusterService;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Cluster;
import com.cloudcheflabs.dataroaster.trino.gateway.service.common.AbstractHibernateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClusterServiceImpl extends AbstractHibernateService<Cluster> implements ClusterService {

    private static Logger LOG = LoggerFactory.getLogger(ClusterServiceImpl.class);

    @Autowired
    private ClusterDao dao;


    public ClusterServiceImpl() {
        super();
    }

    @Override
    protected Operations<Cluster> getDao() {
        return this.dao;
    }

}
