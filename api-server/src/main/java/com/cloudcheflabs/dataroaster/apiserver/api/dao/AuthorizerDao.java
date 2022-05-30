package com.cloudcheflabs.dataroaster.apiserver.api.dao;


import com.cloudcheflabs.dataroaster.apiserver.domain.AuthorizerResponse;

/**
 * Created by mykidong on 2019-08-28.
 */
public interface AuthorizerDao {

    AuthorizerResponse login(String userName, String password);

    AuthorizerResponse getPrivileges(String accessToken);

    AuthorizerResponse removeToken(String accessToken, String refreshToken);

}
