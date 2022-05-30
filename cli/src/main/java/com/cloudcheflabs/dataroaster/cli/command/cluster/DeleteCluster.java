package com.cloudcheflabs.dataroaster.cli.command.cluster;

import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "delete",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Delete Kubernetes Cluster Metadata.")
public class DeleteCluster implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Cluster parent;

    @Override
    public Integer call() throws Exception {
        ConfigProps configProps = parent.configProps;

        java.io.Console cnsl = System.console();

        if (cnsl == null) {
            System.out.println("No console available");
            return -1;
        }

        // show cluster list.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        CommandUtils.showClusterList(configProps);

        String clusterId = cnsl.readLine("Select Cluster ID to be deleted : ");
        while(clusterId.equals("")) {
            System.err.println("cluster id is required!");
            clusterId = cnsl.readLine("Select Cluster ID to be deleted : ");
            if(!clusterId.equals("")) {
                break;
            }
        }

        // create kubernetes cluster.
        return CommandUtils.deleteCluster(configProps, clusterId);
    }
}
