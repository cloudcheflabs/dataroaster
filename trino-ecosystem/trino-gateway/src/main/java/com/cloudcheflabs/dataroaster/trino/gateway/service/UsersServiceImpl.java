package com.cloudcheflabs.dataroaster.trino.gateway.service;

import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.UsersDao;
import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.UsersService;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Users;
import com.cloudcheflabs.dataroaster.trino.gateway.service.common.AbstractHibernateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsersServiceImpl extends AbstractHibernateService<Users> implements UsersService {

    private static Logger LOG = LoggerFactory.getLogger(UsersServiceImpl.class);

    @Autowired
    private UsersDao dao;


    public UsersServiceImpl() {
        super();
    }

    @Override
    protected Operations<Users> getDao() {
        return this.dao;
    }

}
