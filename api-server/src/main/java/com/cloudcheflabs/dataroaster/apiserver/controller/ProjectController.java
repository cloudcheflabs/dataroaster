package com.cloudcheflabs.dataroaster.apiserver.controller;

import com.cloudcheflabs.dataroaster.apiserver.api.service.ProjectService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Roles;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Project;
import com.cloudcheflabs.dataroaster.apiserver.filter.AuthorizationFilter;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
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
public class ProjectController {

    private static Logger LOG = LoggerFactory.getLogger(ProjectController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private HttpServletRequest context;

    @Autowired
    @Qualifier("projectServiceImpl")
    private ProjectService projectService;

    @PostMapping("/apis/project/create")
    public String createProject(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String projectName = params.get("project_name");
            String description = params.get("description");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            projectService.createProject(projectName, description, userName);
            return ControllerUtils.successMessage();
        });
    }

    @PutMapping("/apis/project/update")
    public String updateProject(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String projectId = params.get("project_id");
            String projectName = params.get("project_name");
            String description = params.get("description");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            projectService.updateProject(Long.valueOf(projectId),projectName, description, userName);
            return ControllerUtils.successMessage();
        });
    }


    @DeleteMapping("/apis/project/delete")
    public String deleteProject(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String projectId = params.get("project_id");
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            projectService.deleteProject(Long.valueOf(projectId), userName);
            return ControllerUtils.successMessage();
        });
    }

    @GetMapping("/apis/project/list")
    public String listProject(@RequestParam Map<String, String> params) {
        return ControllerUtils.doProcess(Roles.ROLE_USER, context, () -> {
            String userName = (String) context.getAttribute(AuthorizationFilter.KEY_USER_NAME);

            List<Project> list = projectService.findAll();

            List<Map<String, Object>> mapList = new ArrayList<>();
            for(Project project : list) {
                if(userName.equals(project.getUsers().getUserName())) {
                    long id = project.getId();
                    String name = project.getProjectName();
                    String description = project.getDescription();

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", id);
                    map.put("name", name);
                    map.put("description", description);

                    mapList.add(map);
                }
            }
            return JsonUtils.toJson(mapper, mapList);
        });
    }
}
