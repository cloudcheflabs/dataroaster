package com.cloudcheflabs.dataroaster.cli.command.cicd;

import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create CI / CD.")
public class CreateCiCd implements Callable<Integer> {

    @CommandLine.ParentCommand
    private CiCd parent;

    @CommandLine.Option(names = {"--argocd-ingress-host"}, description = "ArgoCD Ingress Host Name", required = true)
    private String argocdIngressHost;

    @CommandLine.Option(names = {"--jenkins-ingress-host"}, description = "Jenkins Ingress Host Name", required = true)
    private String jenkinsIngressHost;


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

        // create.
        return CommandUtils.createCiCd(
                configProps,
                projectId,
                clusterId,
                argocdIngressHost,
                jenkinsIngressHost,
                storageClass);
    }
}
