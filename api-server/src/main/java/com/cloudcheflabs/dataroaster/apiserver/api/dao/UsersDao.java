package com.cloudcheflabs.dataroaster.apiserver.api.dao;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Users;

public interface UsersDao extends Operations<Users> {
    Users findByUserName(String userName);
}
