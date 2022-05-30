package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.ServicesDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.api.service.ServicesService;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Services;
import com.cloudcheflabs.dataroaster.apiserver.service.common.AbstractHibernateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicesServiceImpl extends AbstractHibernateService<Services> implements ServicesService {

    @Autowired
    private ServicesDao dao;

    public ServicesServiceImpl() {
        super();
    }

    @Override
    protected Operations<Services> getDao() {
        return dao;
    }
}
