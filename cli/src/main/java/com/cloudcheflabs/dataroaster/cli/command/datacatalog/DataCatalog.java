package com.cloudcheflabs.dataroaster.cli.command.datacatalog;

import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.domain.CLIConstants;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = CLIConstants.SERVICE_DATA_CATALOG,
        subcommands = {
                CreateDataCatalog.class,
                DeleteDataCatalog.class,
                CommandLine.HelpCommand.class
        },
        description = "Manage Data Catalog Service.")
public class DataCatalog implements Callable<Integer> {

    ConfigProps configProps;

    @Override
    public Integer call() throws Exception {
        configProps = DataRoasterConfig.getConfigProps();

        return 0;
    }
}
