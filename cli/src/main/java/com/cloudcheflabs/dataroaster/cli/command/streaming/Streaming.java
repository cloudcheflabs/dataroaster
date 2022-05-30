package com.cloudcheflabs.dataroaster.cli.command.streaming;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.CLIConstants;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = CLIConstants.SERVICE_STREAMING,
        subcommands = {
                CreateStreaming.class,
                DeleteStreaming.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Streaming Service.")
public class Streaming implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
