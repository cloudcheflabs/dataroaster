package com.cloudcheflabs.dataroaster.cli.command.kubeconfig;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "kubeconfig",
        subcommands = {
                CreateKubeconfig.class,
                UpdateKubeconfig.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Kubeconfig.")
public class Kubeconfig implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
