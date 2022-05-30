package com.cloudcheflabs.dataroaster.apiserver.api.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Users;

public interface UsersService extends Operations<Users> {
    void signUp(Users users, Roles roles);
    Users findByUserName(String userName);
    void changePassword(String userName, String newPassword);
}
