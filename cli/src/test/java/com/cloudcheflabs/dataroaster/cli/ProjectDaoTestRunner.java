package com.cloudcheflabs.dataroaster.cli;

import com.cloudcheflabs.dataroaster.cli.api.dao.ProjectDao;
import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class ProjectDaoTestRunner {

    @Test
    public void createProject() throws Exception {
        ConfigProps configProps = DataRoasterConfig.getConfigProps();

        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ProjectDao projectDao = applicationContext.getBean(ProjectDao.class);
        RestResponse restResponse = projectDao.createProject(configProps, "any-new-project", "any-new-desc...");

    }
}
