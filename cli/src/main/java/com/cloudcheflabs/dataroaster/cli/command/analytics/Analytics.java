package com.cloudcheflabs.dataroaster.cli.command.analytics;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.CLIConstants;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = CLIConstants.SERVICE_ANALYTICS,
        subcommands = {
                CreateAnalytics.class,
                DeleteAnalytics.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Analytics Service.")
public class Analytics implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
