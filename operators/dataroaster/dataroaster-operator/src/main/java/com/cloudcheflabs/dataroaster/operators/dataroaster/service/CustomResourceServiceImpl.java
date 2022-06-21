package com.cloudcheflabs.dataroaster.operators.dataroaster.service;

import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.CustomResourceDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.CustomResourceService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;
import com.cloudcheflabs.dataroaster.operators.dataroaster.service.common.AbstractHibernateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomResourceServiceImpl extends AbstractHibernateService<CustomResource> implements CustomResourceService {

    private static Logger LOG = LoggerFactory.getLogger(CustomResourceServiceImpl.class);

    @Autowired
    private CustomResourceDao dao;


    public CustomResourceServiceImpl() {
        super();
    }

    @Override
    protected Operations<CustomResource> getDao() {
        return this.dao;
    }

}
