package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.AuthorizerService;
import com.cloudcheflabs.dataroaster.apiserver.domain.AuthorizerResponse;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.domain.Token;
import com.cloudcheflabs.dataroaster.apiserver.util.HttpUtils;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthorizationController {

    private static Logger LOG = LoggerFactory.getLogger(AuthorizationController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    private AuthorizerService authorizerService;

    @PostMapping("/auth/login")
    public String login(@RequestParam Map<String, String> params) {

        String userName = params.get("username");
        String password = params.get("password");
        Token token = authorizerService.login(userName, password);

        AuthorizerResponse authorizerResponse = token.getAuthorizerResponse();
        int statusCode = authorizerResponse.getStatusCode();
        if(statusCode == AuthorizerResponse.STATUS_OK)
        {
            Map<String, Object> map = new HashMap<>();
            map.put("access_token", token.getAccessToken());
            map.put("token_type", token.getTokenType());
            map.put("expires_in", token.getExpiresIn());
            map.put("refresh_token", token.getRefreshToken());

            String json = JsonUtils.toJson(mapper, map);

            return json;
        }
        else
        {
            HttpStatus httpStatus = HttpUtils.getHttpStatus(statusCode);

            throw new ResponseStatusException(httpStatus, authorizerResponse.getErrorMessage());
        }
    }

    @PostMapping("/apis/auth/logout")
    public String logout(@RequestParam Map<String, String> params) {

        String accessToken = params.get("access_token");
        String refreshToken = params.get("refresh_token");

        // role level.
        int allowedRoleLevel = Roles.ROLE_USER.getLevel();

        // max role level of request user.
        int maxRoleLevel = RoleUtils.getMaxRoleLevel(context);

        if(maxRoleLevel >= allowedRoleLevel) {
            AuthorizerResponse authorizerResponse = authorizerService.logout(accessToken, refreshToken);
            int statusCode = authorizerResponse.getStatusCode();
            if(statusCode == AuthorizerResponse.STATUS_OK)
            {
                return authorizerResponse.getSuccessMessage();
            } else {
                HttpStatus httpStatus = HttpUtils.getHttpStatus(statusCode);
                throw new ResponseStatusException(httpStatus, authorizerResponse.getErrorMessage());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "NOT ALLOWED: NO PRIVILEGES");
        }
    }


    @RequestMapping("/public/echo")
    public String echo(@RequestParam(value="message") String message) {

        // role level.
        int allowedRoleLevel = Roles.ROLE_USER.getLevel();

        // max role level of request user.
        int maxRoleLevel = RoleUtils.getMaxRoleLevel(context);

        LOG.debug("maxRoleLevel: {}, allowedRoleLevel: {}", maxRoleLevel, allowedRoleLevel);

        if(maxRoleLevel >= allowedRoleLevel) {
            return "echo [" + message + "] from api server...";
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "NOT ALLOWED: NO PRIVILEGES");
        }
    }
}
