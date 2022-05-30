package com.cloudcheflabs.dataroaster.apiserver.api.service;


import com.cloudcheflabs.dataroaster.apiserver.domain.AuthorizerResponse;
import com.cloudcheflabs.dataroaster.apiserver.domain.Token;

/**
 * Created by mykidong on 2019-08-28.
 */
public interface AuthorizerService {

    Token login(String userName, String password);
    AuthorizerResponse logout(String accessToken, String refreshToken);

}
