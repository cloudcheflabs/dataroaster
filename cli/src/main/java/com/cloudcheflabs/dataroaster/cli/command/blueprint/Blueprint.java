package com.cloudcheflabs.dataroaster.cli.command.blueprint;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "blueprint",
        subcommands = {
                CreateBlueprint.class,
                DeleteBlueprint.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Blueprint Deployment")
public class Blueprint implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
