package com.cloudcheflabs.dataroaster.apiserver.domain;

import java.io.Serializable;

public class Token implements Serializable {

    private AuthorizerResponse authorizerResponse;

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private int expiresIn;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public AuthorizerResponse getAuthorizerResponse() {
        return authorizerResponse;
    }

    public void setAuthorizerResponse(AuthorizerResponse authorizerResponse) {
        this.authorizerResponse = authorizerResponse;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
