package com.cloudcheflabs.dataroaster.trino.gateway.controller;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.ClusterGroupService;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.UsersService;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.Roles;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Users;
import com.cloudcheflabs.dataroaster.trino.gateway.util.BCryptUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    @Qualifier("clusterGroupServiceImpl")
    private ClusterGroupService clusterGroupService;

    @PostMapping("/v1/users/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String user = params.get("user");
            String password = params.get("password");
            String groupName = params.get("group_name");

            // encode password with bcrypt.
            String bcryptEncodedPassword = BCryptUtils.encodeWithBCrypt(password);

            Users users = new Users();
            users.setUser(user);
            users.setPassword(bcryptEncodedPassword);
            users.setClusterGroup(clusterGroupService.findOne(groupName));

            usersService.create(users);
            return ControllerUtils.successMessage();
        });
    }

    @PutMapping("/v1/users/update/password")
    public String updatePassword(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
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
            usersService.deleteById(user);
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
                map.put("groupName", users.getClusterGroup().getGroupName());

                mapList.add(map);
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
