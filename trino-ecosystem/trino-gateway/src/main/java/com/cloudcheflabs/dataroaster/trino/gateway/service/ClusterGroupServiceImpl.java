package com.cloudcheflabs.dataroaster.trino.gateway.service;

import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.ClusterGroupDao;
import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.ClusterGroupService;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.ClusterGroup;
import com.cloudcheflabs.dataroaster.trino.gateway.service.common.AbstractHibernateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClusterGroupServiceImpl extends AbstractHibernateService<ClusterGroup> implements ClusterGroupService {

    private static Logger LOG = LoggerFactory.getLogger(ClusterGroupServiceImpl.class);

    @Autowired
    private ClusterGroupDao dao;


    public ClusterGroupServiceImpl() {
        super();
    }

    @Override
    protected Operations<ClusterGroup> getDao() {
        return this.dao;
    }

}
