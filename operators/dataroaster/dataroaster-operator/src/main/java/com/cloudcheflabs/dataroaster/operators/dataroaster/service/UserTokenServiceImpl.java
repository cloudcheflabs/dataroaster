package com.cloudcheflabs.dataroaster.operators.dataroaster.service;

import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.UserTokenDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.UserTokenService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.UserToken;
import com.cloudcheflabs.dataroaster.operators.dataroaster.service.common.AbstractHibernateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserTokenServiceImpl extends AbstractHibernateService<UserToken> implements UserTokenService {

    private static Logger LOG = LoggerFactory.getLogger(UserTokenServiceImpl.class);

    @Autowired
    private UserTokenDao dao;


    public UserTokenServiceImpl() {
        super();
    }

    @Override
    protected Operations<UserToken> getDao() {
        return this.dao;
    }

}
