package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.UsersDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.api.service.UsersService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.UserAuthorities;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Users;
import com.cloudcheflabs.dataroaster.apiserver.service.common.AbstractHibernateService;
import com.google.common.collect.Sets;
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
        return dao;
    }

    @Override
    public void signUp(Users users, Roles roles) {
        UserAuthorities userAuthorities = new UserAuthorities();
        userAuthorities.setAuthority(roles.name());
        userAuthorities.setUsers(users);

        users.setUserAuthoritiesSet(Sets.newHashSet(userAuthorities));
        dao.create(users);
    }

    @Override
    public Users findByUserName(String userName) {
        return dao.findByUserName(userName);
    }

    @Override
    public void changePassword(String userName, String newPassword) {
        Users users = dao.findByUserName(userName);
        users.setPassword(newPassword);

        dao.update(users);
    }
}
