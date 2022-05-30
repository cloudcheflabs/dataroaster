package com.cloudcheflabs.dataroaster.authorizer.controller;

import com.cloudcheflabs.dataroaster.authorizer.api.service.RoleService;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AuthorizationController {

    private static Logger LOG = LoggerFactory.getLogger(AuthorizationController.class);

    @Autowired
    private RoleService roleService;

    @Autowired
    private HttpServletRequest context;

    @Autowired
    private TokenStore tokenStore;

    private ObjectMapper mapper = new ObjectMapper();


    @RequestMapping("/api/get_privileges")
    public String getPrivileges(@RequestParam(value="service_type") String serviceType) {

        String accessToken = getAccessToken();
        if(accessToken == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Access Token not exist!!");
        }

        if(LOG.isDebugEnabled()) LOG.debug("accessToken: [" + accessToken + "]");

        String userName = getUserName(accessToken);
        if(LOG.isDebugEnabled()) LOG.debug("userName from accessToken: [" + userName + "]");

        try {
            List<String> roles = roleService.getRoles(userName);

            Map<String, List<String>> roleMap = new HashMap<>();
            roleMap.put("role", roles);


            return JsonUtils.toJson(mapper, roleMap);
        }catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping(value = "/remove_token")
    public String removeToken(@RequestParam Map<String, String> params) {

        String acccessToken = getAccessToken();
        String refreshToken = params.get("refresh_token");

        try {
            OAuth2RefreshToken oAuth2RefreshToken = tokenStore.readRefreshToken(refreshToken);

            if (acccessToken != null) {
                OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(acccessToken);
                tokenStore.removeAccessToken(oAuth2AccessToken);
                if (LOG.isDebugEnabled()) LOG.debug("access token: [{}] removed...", acccessToken);
            } else {
                tokenStore.removeAccessTokenUsingRefreshToken(oAuth2RefreshToken);
            }

            tokenStore.removeRefreshToken(oAuth2RefreshToken);
        }catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        return "{ 'result': 'SUCCESS'}";
    }

    private String getAccessToken()
    {
        String accessToken = null;

        String bearerToken = context.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            accessToken = bearerToken.substring(7, bearerToken.length());
        }
        else
        {
            return null;
        }

        return accessToken;
    }


    private String getUserName(String accessToken)
    {
        JsonParser parser = JsonParserFactory.getJsonParser();
        Map<String, ?> tokenData = parser.parseMap(JwtHelper.decode(accessToken).getClaims());

        return  (String) tokenData.get("user_name");
    }
}
