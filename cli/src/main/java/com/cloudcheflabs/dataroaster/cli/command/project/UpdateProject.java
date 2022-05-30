package com.cloudcheflabs.dataroaster.cli.command.project;

import com.cloudcheflabs.dataroaster.cli.api.dao.ProjectDao;
import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "update",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Update Project.")
public class UpdateProject implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Project parent;

    @Override
    public Integer call() throws Exception {
        ConfigProps configProps = parent.configProps;

        java.io.Console cnsl = System.console();

        if (cnsl == null) {
            System.out.println("No console available");
            return -1;
        }

        // show project list.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        CommandUtils.showProjectList(configProps);

        String projectId = cnsl.readLine("Select Project ID to be updated : ");
        while(projectId.equals("")) {
            System.err.println("project id is required!");
            projectId = cnsl.readLine("Select Project ID to be updated : ");
            if(!projectId.equals("")) {
                break;
            }
        }
      
        String name = cnsl.readLine("Enter Project Name : ");
        while(name.equals("")) {
            System.err.println("project name is required!");
            name = cnsl.readLine("Enter Project Name : ");
            if(!name.equals("")) {
                break;
            }
        }       

        String description = cnsl.readLine("Enter Project Description : ");
        while(description.equals("")) {
            System.err.println("project description is required!");
            description = cnsl.readLine("Enter Project Description : ");
            if(!description.equals("")) {
                break;
            }
        }
      
        // update project.
        ProjectDao projectDao = applicationContext.getBean(ProjectDao.class);
        RestResponse restResponse = projectDao.updateProject(configProps, Long.valueOf(projectId), name, description);

        if(restResponse.getStatusCode() == 200) {
            System.out.println("project updated successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }
}
