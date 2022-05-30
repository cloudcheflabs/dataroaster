package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.ServiceDefService;
import com.cloudcheflabs.dataroaster.apiserver.api.service.ServicesService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.ServiceDef;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Services;
import com.cloudcheflabs.dataroaster.apiserver.filter.AuthorizationFilter;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ServicesController {

    private static Logger LOG = LoggerFactory.getLogger(ServicesController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("serviceDefServiceImpl")
    private ServiceDefService serviceDefService;


    @Autowired
    @Qualifier("servicesServiceImpl")
    private ServicesService servicesService;


    @GetMapping("/apis/service_def/list")
    public String listServiceDef(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            List<ServiceDef> list = serviceDefService.findAll();

            List<Map<String, Object>> mapList = new ArrayList<>();
            for(ServiceDef serviceDef : list) {
                long id = serviceDef.getId();
                String type = serviceDef.getType();
                String name = serviceDef.getName();

                Map<String, Object> map = new HashMap<>();
                map.put("id", id);
                map.put("type", type);
                map.put("name", name);

                mapList.add(map);
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }


    @GetMapping("/apis/services/list")
    public String listServices(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            List<Services> list = servicesService.findAll();

            List<Map<String, Object>> mapList = new ArrayList<>();
            for(Services services : list) {
                if(services.getProject().getUsers().getUserName().equals(userName)) {
                    String projectName = services.getProject().getProjectName();
                    String clusterName = services.getK8sNamespace().getK8sCluster().getClusterName();

                    long id = services.getId();
                    String serviceDefType = services.getServiceDef().getType();

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", id);
                    map.put("serviceDefType", serviceDefType);
                    map.put("projectName", projectName);
                    map.put("clusterName", clusterName);

                    mapList.add(map);
                }
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
