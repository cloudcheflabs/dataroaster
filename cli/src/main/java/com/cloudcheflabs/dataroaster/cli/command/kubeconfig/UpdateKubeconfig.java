package com.cloudcheflabs.dataroaster.cli.command.kubeconfig;

import com.cloudcheflabs.dataroaster.cli.api.dao.KubeconfigDao;
import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "update",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Update Kubeconfig.")
public class UpdateKubeconfig implements Callable<Integer> {

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
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        CommandUtils.showClusterList(configProps);

        String clusterId = CommandUtils.getClusterIdByPrompt(cnsl);

        // create kubeconfig.
        String kubeconfigPath = kubeconfigFile.getAbsolutePath();
        String kubeconfig = FileUtils.fileToString(kubeconfigPath, false);

        KubeconfigDao kubeconfigDao = applicationContext.getBean(KubeconfigDao.class);
        RestResponse restResponse = kubeconfigDao.updateKubeconfig(configProps, Long.valueOf(clusterId), kubeconfig);
        if(restResponse.getStatusCode() == 200) {
            System.out.println("kubeconfig updated successfully!");
            return 0;
        } else {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }
    }
}
