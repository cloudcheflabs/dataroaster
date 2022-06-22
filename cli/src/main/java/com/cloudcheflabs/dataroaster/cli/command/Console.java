package com.cloudcheflabs.dataroaster.cli.command;

import com.cloudcheflabs.dataroaster.cli.command.analytics.Analytics;
import com.cloudcheflabs.dataroaster.cli.command.backup.Backup;
import com.cloudcheflabs.dataroaster.cli.command.blueprint.Blueprint;
import com.cloudcheflabs.dataroaster.cli.command.cicd.CiCd;
import com.cloudcheflabs.dataroaster.cli.command.cluster.Cluster;
import com.cloudcheflabs.dataroaster.cli.command.datacatalog.DataCatalog;
import com.cloudcheflabs.dataroaster.cli.command.distributedtracing.DistributedTracing;
import com.cloudcheflabs.dataroaster.cli.command.ingresscontroller.IngressController;
import com.cloudcheflabs.dataroaster.cli.command.kubeconfig.Kubeconfig;
import com.cloudcheflabs.dataroaster.cli.command.login.Login;
import com.cloudcheflabs.dataroaster.cli.command.metricsmonitoring.MetricsMonitoring;
import com.cloudcheflabs.dataroaster.cli.command.podlogmonitoring.PodLogMonitoring;
import com.cloudcheflabs.dataroaster.cli.command.privateregistry.PrivateRegistry;
import com.cloudcheflabs.dataroaster.cli.command.project.Project;
import com.cloudcheflabs.dataroaster.cli.command.queryengine.QueryEngine;
import com.cloudcheflabs.dataroaster.cli.command.streaming.Streaming;
import com.cloudcheflabs.dataroaster.cli.command.workflow.Workflow;
import picocli.CommandLine;


@CommandLine.Command(name = "dataroaster",
        subcommands = {
                Login.class,
                Cluster.class,
                Kubeconfig.class,
                Project.class,
                IngressController.class,
                PodLogMonitoring.class,
                MetricsMonitoring.class,
                DistributedTracing.class,
                PrivateRegistry.class,
                CiCd.class,
                Backup.class,
                DataCatalog.class,
                QueryEngine.class,
                Streaming.class,
                Analytics.class,
                Workflow.class,
                Blueprint.class,
                CommandLine.HelpCommand.class
        },
        version = "dataroaster 4.1.0-SNAPSHOT",
        description = "DataRoaster CLI Console.")
public class Console implements Runnable {

    @Override
    public void run() { }
}
