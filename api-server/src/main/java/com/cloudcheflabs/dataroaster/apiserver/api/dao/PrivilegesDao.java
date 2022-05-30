package com.cloudcheflabs.dataroaster.apiserver.api.dao;


import com.cloudcheflabs.dataroaster.apiserver.domain.Privileges;

/**
 * Created by mykidong on 2019-08-28.
 */
public interface PrivilegesDao {

    void putPrivileges(String accessKey, Privileges privileges);

    Privileges getPrivileges(String accessToken);
}
