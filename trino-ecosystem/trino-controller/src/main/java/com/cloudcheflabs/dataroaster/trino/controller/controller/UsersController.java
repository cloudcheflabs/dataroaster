package com.cloudcheflabs.dataroaster.trino.controller.controller;

import com.cloudcheflabs.dataroaster.trino.controller.api.service.RegisterClusterService;
import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;
import com.cloudcheflabs.dataroaster.trino.controller.domain.Roles;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class UsersController implements InitializingBean {

    private static Logger LOG = LoggerFactory.getLogger(UsersController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("registerClusterServiceImpl")
    private RegisterClusterService registerClusterService;

    private String trinoGatewayRestUri;

    @Override
    public void afterPropertiesSet() throws Exception {
        trinoGatewayRestUri = env.getProperty("trino.gateway.restUri");
    }


    @PostMapping("/v1/users/create")
    public String create(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String user = params.get("user");
            String password = params.get("password");
            String groupName = params.get("group_name");

            registerClusterService.createUser(trinoGatewayRestUri, user, password, groupName);

            return ControllerUtils.successMessage();
        });
    }

    @PutMapping("/v1/users/update/password")
    public String updatePassword(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String user = params.get("user");
            String password = params.get("password");

            registerClusterService.updatePassword(trinoGatewayRestUri, user, password);

            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/v1/users/delete")
    public String delete(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            String user = params.get("user");

            registerClusterService.deleteUser(trinoGatewayRestUri, user);

            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/v1/users/list")
    public String list(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_PLATFORM_ADMIN, context, () -> {
            RestResponse restResponse = registerClusterService.listUsers(trinoGatewayRestUri);
            return (restResponse.getStatusCode() == 200) ? restResponse.getSuccessMessage() : restResponse.getErrorMessage();
        });
    }
}
