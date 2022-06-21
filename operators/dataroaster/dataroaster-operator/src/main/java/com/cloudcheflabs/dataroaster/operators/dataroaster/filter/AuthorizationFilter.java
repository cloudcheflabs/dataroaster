package com.cloudcheflabs.dataroaster.operators.dataroaster.filter;

import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.UserTokenService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.Privileges;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.Roles;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.UserToken;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthorizationFilter implements Filter {

    private static Logger LOG = LoggerFactory.getLogger(AuthorizationFilter.class);

    public static final String KEY_PRIVILEGES = "privileges";

    @Autowired
    @Qualifier("userTokenServiceImpl")
    private UserTokenService userTokenService;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String uri = req.getRequestURI();

        // login request.
        if(uri.contains("/login")) {
            List<String> roleList = Arrays.asList(Roles.ROLE_USER.name());
            Privileges privileges = new Privileges(roleList);
            request.setAttribute(KEY_PRIVILEGES, privileges);
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");
        if(authHeader != null) {
            String[] headerTokens = authHeader.split(" ");
            String bearer = headerTokens[0];
            String token = headerTokens[1];
            long now = DateTimeUtils.currentTimeMillis();

            // authenticate with token.
            UserToken userToken = userTokenService.findOne(token);
            if(userToken != null) {
                long expiration = userToken.getExpiration();
                if(now > expiration) {
                    throw new ServletException("token [" + token + "] expired!");
                }
            } else {
                throw new ServletException("token [" + token + "] not found!");
            }

            // TODO: platform admin role must be changed in the future!!!
            List<String> roleList = Arrays.asList(Roles.ROLE_PLATFORM_ADMIN.name());
            Privileges privileges = new Privileges(roleList);
            request.setAttribute(KEY_PRIVILEGES, privileges);
            chain.doFilter(request, response);
        } else {
            throw new ServletException("Token not found in Authorization header!");
        }
    }
}
