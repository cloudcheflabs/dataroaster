package com.cloudcheflabs.dataroaster.apiserver.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.cloudcheflabs.dataroaster.apiserver.api.service.UsersService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class UsersController {

    private static Logger LOG = LoggerFactory.getLogger(UsersController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("usersServiceImpl")
    private UsersService usersService;


    @PostMapping("/signup")
    public String signUp(@RequestParam Map<String, String> params) {

        String userName = params.get("username");
        String password = params.get("password");
        String role = params.get("role");

        // ROLE_PLATFORM_ADMIN: 1000
        // ROLE_USER: 10
        int roleInt = Integer.parseInt(role);

        Roles userRole = null;
        for(Roles roles : Roles.values()) {
            if(roleInt == roles.getLevel()) {
                userRole = roles;
                break;
            }
        }
        if(userRole == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role [" + roleInt + "] does not exist!");
        }

        // password encoding with bcrypt.
        password = BCrypt.withDefaults().hashToString(8, password.toCharArray());

        Users users = new Users();
        users.setUserName(userName);
        users.setPassword(password);
        users.setEnabled(true);

        usersService.signUp(users, userRole);

        return ControllerUtils.successMessage();
    }

    @PutMapping("/apis/users/change_password")
    public String changePassword(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String userName = params.get("username");
            String newPassword = params.get("new_password");
            // password encoding with bcrypt.
            newPassword = BCrypt.withDefaults().hashToString(8, newPassword.toCharArray());

            usersService.changePassword(userName, newPassword);
            return ControllerUtils.successMessage();
        });
    }

    @DeleteMapping("/apis/users/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String id = params.get("id");

            Users users = usersService.findOne(Long.valueOf(id));
            usersService.delete(users);
            return ControllerUtils.successMessage();
        });
    }
}
