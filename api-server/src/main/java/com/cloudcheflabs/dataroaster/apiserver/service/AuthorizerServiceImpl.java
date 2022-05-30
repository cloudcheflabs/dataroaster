package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.AuthorizerDao;
import com.cloudcheflabs.dataroaster.apiserver.api.service.AuthorizerService;
import com.cloudcheflabs.dataroaster.apiserver.domain.AuthorizerResponse;
import com.cloudcheflabs.dataroaster.apiserver.domain.Token;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthorizerServiceImpl implements AuthorizerService {

    private static Logger LOG = LoggerFactory.getLogger(PrivilegesServiceImpl.class);

    @Autowired
    private AuthorizerDao authorizerDao;
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public Token login(String userName, String password) {

        Token token = new Token();

        AuthorizerResponse authorizerResponse = authorizerDao.login(userName, password);

        token.setAuthorizerResponse(authorizerResponse);

        if(authorizerResponse.getStatusCode() == AuthorizerResponse.STATUS_OK)
        {
            LOG.debug("tokens: \n{}", JsonWriter.formatJson(authorizerResponse.getSuccessMessage()));

            Map<String, Object> map = JsonUtils.toMap(mapper, authorizerResponse.getSuccessMessage());
            String accessToken = (String) map.get("access_token");
            String refreshToken = (String) map.get("refresh_token");
            String tokenType = (String) map.get("token_type");
            int expiresIn = (Integer) map.get("expires_in");

            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setTokenType(tokenType);
            token.setExpiresIn(expiresIn);
        }

        return token;
    }

    @Override
    public AuthorizerResponse logout(String accessToken, String refreshToken) {
        return authorizerDao.removeToken(accessToken, refreshToken);
    }
}
