package com.cloudcheflabs.dataroaster.cli.command.cicd;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.CLIConstants;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = CLIConstants.SERVICE_CICD,
        subcommands = {
                CreateCiCd.class,
                DeleteCiCd.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage CI / CD Service.")
public class CiCd implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
