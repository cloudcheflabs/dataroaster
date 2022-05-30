package com.cloudcheflabs.dataroaster.apiserver.filter;

import com.cloudcheflabs.dataroaster.apiserver.api.service.PrivilegesService;
import com.cloudcheflabs.dataroaster.apiserver.config.FilterConfigurer;
import com.cloudcheflabs.dataroaster.apiserver.domain.AuthorizerResponse;
import com.cloudcheflabs.dataroaster.apiserver.domain.Privileges;
import com.cloudcheflabs.dataroaster.apiserver.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@Component
public class AuthorizationFilter implements Filter {

    private static Logger LOG = LoggerFactory.getLogger(FilterConfigurer.class);

    public static final String KEY_PRIVILEGES = "privileges";
    public static final String KEY_USER_NAME = "userName";


    @Autowired
    private PrivilegesService privilegesService;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String accessToken = getAccessToken(req);
        if(accessToken == null)
        {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Token Not Found!");
        }

        Privileges privileges = privilegesService.getPrivileges(accessToken);

        AuthorizerResponse authorizerResponse = privileges.getAuthorizerResponse();
        int statusCode = authorizerResponse.getStatusCode();
        LOG.debug("statusCode: {}", statusCode);
        if(statusCode != AuthorizerResponse.STATUS_OK)
        {
            // Access Token 이 만료되었을 경우임으로 Error Response 를 보내고 다시 Login API 를 Call 해서 새로운 Access Token 을 받도록 함.
            HttpStatus httpStatus = HttpUtils.getHttpStatus(statusCode);

            throw new ResponseStatusException(httpStatus, authorizerResponse.getErrorMessage());
        }
        else {
            String userName = getUserName(accessToken);
            request.setAttribute(KEY_USER_NAME, userName);
            request.setAttribute(KEY_PRIVILEGES, privileges);
            chain.doFilter(request, response);
        }
    }

    private String getAccessToken(HttpServletRequest req)
    {
        String accessToken = null;

        String bearerToken = req.getHeader("Authorization");

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
