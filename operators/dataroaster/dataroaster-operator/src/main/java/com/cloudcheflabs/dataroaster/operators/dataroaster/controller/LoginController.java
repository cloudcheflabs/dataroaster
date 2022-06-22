package com.cloudcheflabs.dataroaster.operators.dataroaster.controller;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.UserTokenService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.UsersService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.Roles;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.UserToken;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.Users;
import com.cloudcheflabs.dataroaster.operators.dataroaster.util.BCryptUtils;
import com.cloudcheflabs.dataroaster.operators.dataroaster.util.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.User;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
public class LoginController {

    private static Logger LOG = LoggerFactory.getLogger(LoginController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("usersServiceImpl")
    private UsersService usersService;

    @Autowired
    @Qualifier("userTokenServiceImpl")
    private UserTokenService userTokenService;


    @PostMapping("/v1/login")
    public String login(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String user = params.get("user");
            String password = params.get("password");
            LOG.info("password: [{}]", password);

            Users users = usersService.findOne(user);
            if(users != null) {
                String bcryptedPassword = users.getPassword();
                boolean isMatched = BCryptUtils.isMatched(password, bcryptedPassword);
                if(isMatched) {
                    Set<UserToken> userTokenSet = users.getUserTokenSet();
                    String token = null;
                    long expiration = -1;

                    if(userTokenSet.size() == 0) {
                        // create token and save it with expiration.
                        UserToken userToken = createNewToken(users, userTokenService);
                        token = userToken.getToken();
                        expiration = userToken.getExpiration();
                    } else {
                        long now = DateTimeUtils.currentTimeMillis();
                        UserToken userTokenTemp = Arrays.asList(userTokenSet.toArray(new UserToken[0])).get(0);
                        long expirationTemp = userTokenTemp.getExpiration();
                        if(now > expirationTemp) {
                            userTokenService.delete(userTokenTemp);
                            
                            // create new one.
                            UserToken userToken = createNewToken(users, userTokenService);
                            token = userToken.getToken();
                            expiration = userToken.getExpiration();
                        } else {
                            token = userTokenTemp.getToken();
                            expiration = userTokenTemp.getExpiration();
                        }
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put("token", token);
                    map.put("expiration", new DateTime(expiration).toString());

                    return JsonUtils.toJson(mapper, map);
                } else {
                    throw new IllegalAccessException("Authentication failed!");
                }
            } else {
                throw new IllegalStateException("user [" + user + "] does not exist!");
            }
        });
    }

    private static UserToken createNewToken(Users users, UserTokenService userTokenService) {
        String newToken = TokenUtils.newToken();
        DateTime dt = new DateTime(DateTimeUtils.currentTimeMillis()).plusHours(UserToken.EXPIRATION_IN_HOUR);
        long expiration = dt.getMillis();

        UserToken userToken = new UserToken();
        userToken.setToken(newToken);
        userToken.setExpiration(expiration);
        userToken.setUsers(users);
        userTokenService.create(userToken);

        return userToken;
    }
}
