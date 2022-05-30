package com.cloudcheflabs.dataroaster.cli.command.podlogmonitoring;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.CLIConstants;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = CLIConstants.SERVICE_POD_LOG_MONITORING,
        subcommands = {
                CreatePodLogMonitoring.class,
                DeletePodLogMonitoring.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Pod Log Monitoring Service.")
public class PodLogMonitoring implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
