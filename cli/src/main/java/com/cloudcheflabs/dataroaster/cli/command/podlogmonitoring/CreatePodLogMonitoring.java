package com.cloudcheflabs.dataroaster.cli.command.podlogmonitoring;

import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Pod Log Monitoring.")
public class CreatePodLogMonitoring implements Callable<Integer> {

    @CommandLine.ParentCommand
    private PodLogMonitoring parent;

    @CommandLine.Option(names = {"--elasticsearch-hosts"}, description = "Elasticsearch Hosts", required = true)
    private String elasticsearchHosts;

    @Override
    public Integer call() throws Exception {
        ConfigProps configProps = parent.configProps;

        java.io.Console cnsl = System.console();
        if (cnsl == null) {
            System.out.println("No console available");
            return -1;
        }

        // show project list.
        CommandUtils.showProjectList(configProps);

        System.out.printf("\n");

        String projectId = CommandUtils.getProjectIdByPrompt(cnsl);
       
        System.out.printf("\n");


        // show cluster list.
        CommandUtils.showClusterList(configProps);

        System.out.printf("\n");

        String clusterId = CommandUtils.getClusterIdByPrompt(cnsl);

        System.out.printf("\n");

        // create.
        return CommandUtils.createPodLogMonitoring(
                configProps,
                projectId,
                clusterId,
                elasticsearchHosts);
    }
}
