package com.cloudcheflabs.dataroaster.trino.controller.filter;

import com.cloudcheflabs.dataroaster.trino.controller.domain.Privileges;
import com.cloudcheflabs.dataroaster.trino.controller.domain.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthorizationFilter implements Filter {

    private static Logger LOG = LoggerFactory.getLogger(AuthorizationFilter.class);

    public static final String KEY_PRIVILEGES = "privileges";


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // TODO: check access token retrieved from request, if there is no token(jwt token)

        // TODO: get user privileges with connecting to OAuth2 server.
        // NOTE: platform admin role must be changed in the future!!!
        List<String> roleList = Arrays.asList(Roles.ROLE_PLATFORM_ADMIN.name());
        Privileges privileges = new Privileges(roleList);
        request.setAttribute(KEY_PRIVILEGES, privileges);
        chain.doFilter(request, response);
    }
}
