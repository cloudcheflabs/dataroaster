package com.cloudcheflabs.dataroaster.cli.command.backup;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.CLIConstants;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = CLIConstants.SERVICE_BACKUP,
        subcommands = {
                CreateBackup.class,
                DeleteBackup.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Backup Service.")
public class Backup implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
