package com.cloudcheflabs.dataroaster.cli.command.privateregistry;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.CLIConstants;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = CLIConstants.SERVICE_PRIVATE_REGISTRY,
        subcommands = {
                CreatePrivateRegistry.class,
                DeletePrivateRegistry.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Private Registry Service.")
public class PrivateRegistry implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
