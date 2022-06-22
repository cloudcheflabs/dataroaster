package com.cloudcheflabs.dataroaster.operators.dataroaster.controller;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.UserTokenService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.UsersService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.Roles;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.UserToken;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.Users;
import com.cloudcheflabs.dataroaster.operators.dataroaster.util.BCryptUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class UsersController {

    private static Logger LOG = LoggerFactory.getLogger(UsersController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("usersServiceImpl")
    private UsersService usersService;

    @Autowired
    @Qualifier("userTokenServiceImpl")
    private UserTokenService userTokenService;


    @PostMapping("/v1/users/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String user = params.get("user");
            String password = params.get("password");

            // encode password with bcrypt.
            String bcryptEncodedPassword = BCryptUtils.encodeWithBCrypt(password);

            Users users = new Users();
            users.setUser(user);
            users.setPassword(bcryptEncodedPassword);

            usersService.create(users);
            return ControllerUtils.successMessage();
        });
    }

    @PutMapping("/v1/users/update/password")
    public String updatePassword(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String user = params.get("user");
            String password = params.get("password");

            // encode password with bcrypt.
            String bcryptEncodedPassword = BCryptUtils.encodeWithBCrypt(password);

            Users users = usersService.findOne(user);
            if(users != null) {
                users.setPassword(bcryptEncodedPassword);
                usersService.update(users);
            } else {
                throw new IllegalStateException("user [" + user + "] not found!");
            }
            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/v1/users/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String user = params.get("user");
            Users users = usersService.findOne(user);
            Set<UserToken> userTokenSet = users.getUserTokenSet();
            for(UserToken userToken : userTokenSet) {
                userTokenService.delete(userToken);
            }
            usersService.delete(users);
            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/users/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            List<Map<String, Object>> mapList = new ArrayList<>();

            List<Users> lists = usersService.findAll();
            for(Users users : lists) {
                Map<String, Object> map = new HashMap<>();
                map.put("user", users.getUser());

                mapList.add(map);
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
