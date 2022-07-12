package com.cloudcheflabs.dataroaster.trino.controller.controller;

import com.cloudcheflabs.dataroaster.trino.controller.domain.Roles;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class UsersController {

    private static Logger LOG = LoggerFactory.getLogger(UsersController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;


    @PostMapping("/v1/users/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String user = params.get("user");
            String password = params.get("password");
            String groupName = params.get("group_name");

            return ControllerUtils.successMessage();
        });
    }

    @PutMapping("/v1/users/update/password")
    public String updatePassword(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String user = params.get("user");
            String password = params.get("password");

            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/v1/users/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String user = params.get("user");

            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/users/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            // TODO
            String json = "";

            return json;
        });
    }
}
