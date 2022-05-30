package com.cloudcheflabs.dataroaster.apiserver.api.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Project;

public interface ProjectService extends Operations<Project> {
    void createProject(String projectName, String description, String userName);
    void updateProject(long id, String projectName, String description, String userName);
    void deleteProject(long id, String userName);
}
