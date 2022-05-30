package com.cloudcheflabs.dataroaster.apiserver.domain;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Privileges implements Serializable{

    private AuthorizerResponse authorizerResponse;
    private List<String> roleList;
    private ObjectMapper mapper = new ObjectMapper();

    public AuthorizerResponse getAuthorizerResponse() {
        return authorizerResponse;
    }

    public void setAuthorizerResponse(AuthorizerResponse authorizerResponse) {
        this.authorizerResponse = authorizerResponse;

        if(this.authorizerResponse.getStatusCode() == AuthorizerResponse.STATUS_OK) {
            Map<String, Object> map = JsonUtils.toMap(mapper, this.authorizerResponse.getSuccessMessage());
            roleList = (List<String>) map.get("role");
        }
    }

    public List<String> getRoleList() {
        return this.roleList;
    }
}
