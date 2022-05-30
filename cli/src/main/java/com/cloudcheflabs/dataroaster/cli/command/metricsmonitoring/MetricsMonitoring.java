package com.cloudcheflabs.dataroaster.cli.command.metricsmonitoring;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.CLIConstants;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = CLIConstants.SERVICE_METRICS_MONITORING,
        subcommands = {
                CreateMetricsMonitoring.class,
                DeleteMetricsMonitoring.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Metrics Monitoring Service.")
public class MetricsMonitoring implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
