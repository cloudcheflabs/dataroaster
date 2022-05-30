package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface ProjectDao {
    RestResponse createProject(ConfigProps configProps, String name, String description);
    RestResponse updateProject(ConfigProps configProps, long id, String name, String description);
    RestResponse deleteProject(ConfigProps configProps, long id);
    RestResponse listProjects(ConfigProps configProps);
}
