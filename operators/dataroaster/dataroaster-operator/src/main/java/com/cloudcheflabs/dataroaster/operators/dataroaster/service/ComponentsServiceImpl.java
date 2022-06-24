package com.cloudcheflabs.dataroaster.operators.dataroaster.service;

import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.ComponentsDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.ComponentsService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.Components;
import com.cloudcheflabs.dataroaster.operators.dataroaster.service.common.AbstractHibernateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ComponentsServiceImpl extends AbstractHibernateService<Components> implements ComponentsService {

    private static Logger LOG = LoggerFactory.getLogger(ComponentsServiceImpl.class);

    @Autowired
    private ComponentsDao dao;


    public ComponentsServiceImpl() {
        super();
    }

    @Override
    protected Operations<Components> getDao() {
        return this.dao;
    }

}
