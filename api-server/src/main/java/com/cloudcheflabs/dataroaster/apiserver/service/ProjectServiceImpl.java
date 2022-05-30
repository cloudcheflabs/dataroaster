package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.ProjectDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.UsersDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.api.service.ProjectService;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Project;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Users;
import com.cloudcheflabs.dataroaster.apiserver.service.common.AbstractHibernateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectServiceImpl extends AbstractHibernateService<Project> implements ProjectService {

    private static Logger LOG = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Autowired
    private ProjectDao dao;

    @Autowired
    @Qualifier("hibernateUsersDao")
    private UsersDao usersDao;

    public ProjectServiceImpl() {
        super();
    }

    @Override
    protected Operations<Project> getDao() {
        return this.dao;
    }

    @Override
    public void createProject(String projectName, String description, String userName) {
        // get user.
        Users users = usersDao.findByUserName(userName);

        Project project = new Project();
        project.setProjectName(projectName);
        project.setDescription(description);
        project.setUsers(users);

        dao.create(project);
    }

    @Override
    public void updateProject(long id, String projectName, String description, String userName) {
        Project project = dao.findOne(id);

        String originalCreator = project.getUsers().getUserName();
        if(!userName.equals(originalCreator)) {
            throw new RuntimeException("user [" + userName + "] not allowed to update.");
        }

        project.setProjectName(projectName);
        project.setDescription(description);

        dao.update(project);
    }

    @Override
    public void deleteProject(long id, String userName) {
        Project project = dao.findOne(id);

        String originalCreator = project.getUsers().getUserName();
        if(!userName.equals(originalCreator)) {
            throw new RuntimeException("user [" + userName + "] not allowed to delete.");
        }

        dao.delete(project);
    }
}
