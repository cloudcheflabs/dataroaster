package com.cloudcheflabs.dataroaster.authorizer.service;

import com.cloudcheflabs.dataroaster.authorizer.api.dao.UserDetailsDao;
import com.cloudcheflabs.dataroaster.authorizer.api.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private UserDetailsDao userDetailsDao;

    @Override
    public List<String> getRoles(String userName) {
        return userDetailsDao.getRoles(userName);
    }
}
