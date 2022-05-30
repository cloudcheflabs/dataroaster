package com.cloudcheflabs.dataroaster.cli.command.kubeconfig;

import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Create Kubeconfig.")
public class CreateKubeconfig implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Kubeconfig parent;

    @CommandLine.Option(names = {"--kubeconfig"}, description = "Kubeconfig File Path.", required = true)
    private File kubeconfigFile;

    @Override
    public Integer call() throws Exception {
        ConfigProps configProps = parent.configProps;

        java.io.Console cnsl = System.console();
        if (cnsl == null) {
            System.out.println("No console available");
            return -1;
        }

        // show cluster list.
        CommandUtils.showClusterList(configProps);

        String clusterId = CommandUtils.getClusterIdByPrompt(cnsl);

        // create kubeconfig.
        String kubeconfigPath = kubeconfigFile.getAbsolutePath();
        String kubeconfig = FileUtils.fileToString(kubeconfigPath, false);

        return CommandUtils.createKubeconfig(configProps, clusterId, kubeconfig);
    }
}
