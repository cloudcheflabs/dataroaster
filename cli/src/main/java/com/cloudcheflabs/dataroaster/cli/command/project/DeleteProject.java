package com.cloudcheflabs.dataroaster.cli.command.project;

import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "delete",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Delete Project.")
public class DeleteProject implements Callable<Integer> {

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

        String projectId = cnsl.readLine("Select Project ID to be deleted : ");
        while(projectId.equals("")) {
            System.err.println("project id is required!");
            projectId = cnsl.readLine("Select Project ID to be deleted : ");
            if(!projectId.equals("")) {
                break;
            }
        }
       
        // delete project.
        return CommandUtils.deleteProject(configProps, projectId);
    }
}
