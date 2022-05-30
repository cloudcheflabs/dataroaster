package com.cloudcheflabs.dataroaster.cli.command.project;

import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Project.")
public class CreateProject implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Project parent;

    @CommandLine.Option(names = {"--name"}, description = "Project Name", required = true)
    private String name;

    @CommandLine.Option(names = {"--description"}, description = "Project Description", required = true)
    private String description;

    @Override
    public Integer call() throws Exception {
        ConfigProps configProps = parent.configProps;

        return CommandUtils.createProject(configProps, name, description);
    }
}
