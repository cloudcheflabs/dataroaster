package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.ServiceDefDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.api.service.ServiceDefService;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.ServiceDef;
import com.cloudcheflabs.dataroaster.apiserver.service.common.AbstractHibernateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServiceDefServiceImpl extends AbstractHibernateService<ServiceDef> implements ServiceDefService {

    @Autowired
    private ServiceDefDao dao;

    public ServiceDefServiceImpl() {
        super();
    }

    @Override
    protected Operations<ServiceDef> getDao() {
        return dao;
    }
}
