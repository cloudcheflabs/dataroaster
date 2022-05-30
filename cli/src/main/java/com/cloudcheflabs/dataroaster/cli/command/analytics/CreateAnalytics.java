package com.cloudcheflabs.dataroaster.cli.command.analytics;

import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Analytics.")
public class CreateAnalytics implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Analytics parent;

    @CommandLine.Option(names = {"--jupyterhub-github-client-id"}, description = "Jupyterhub GitHub OAuth Client ID.", required = true)
    private String jupyterhubGithubClientId;

    @CommandLine.Option(names = {"--jupyterhub-github-client-secret"}, description = "Jupyterhub GitHub OAuth Client Secret.", required = true)
    private String jupyterhubGithubClientSecret;

    @CommandLine.Option(names = {"--jupyterhub-ingress-host"}, description = "Jupyterhub Ingress Host.", required = true)
    private String jupyterhubIngressHost;

    @CommandLine.Option(names = {"--jupyterhub-storage-size"}, description = "Jupyterhub Storage Size in GiB.", required = true)
    private int jupyterhubStorageSize;

    @CommandLine.Option(names = {"--redash-storage-size"}, description = "Redash Storage Size in GiB.", required = true)
    private int redashStorageSize;

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

        String projectId = CommandUtils.getProjectIdByPrompt(cnsl);

        System.out.printf("\n");


        // show cluster list.
        CommandUtils.showClusterList(configProps);

        System.out.printf("\n");

        String clusterId = CommandUtils.getClusterIdByPrompt(cnsl);

        System.out.printf("\n");

        // show storage classes.
        CommandUtils.showStorageClasses(configProps, clusterId);

        System.out.printf("\n");

        String storageClass = CommandUtils.getStorageClassByPrompt(cnsl);
       

        System.out.printf("\n");

        return CommandUtils.createAnalytics(
                configProps,
                projectId,
                clusterId,
                jupyterhubGithubClientId,
                jupyterhubGithubClientSecret,
                jupyterhubIngressHost,
                storageClass,
                Integer.valueOf(jupyterhubStorageSize),
                Integer.valueOf(redashStorageSize)
        );
    }
}
