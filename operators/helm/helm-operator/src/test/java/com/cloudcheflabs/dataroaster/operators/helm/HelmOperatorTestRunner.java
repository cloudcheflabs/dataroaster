package com.cloudcheflabs.dataroaster.operators.helm;


import com.cloudcheflabs.dataroaster.operators.helm.crd.HelmChart;
import com.cloudcheflabs.dataroaster.operators.helm.handler.*;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HelmOperatorTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(HelmOperatorTestRunner.class);

    @Test
    public void runMain() throws Exception {
        // helm chart kubernetes client.
        HelmChartClient helmChartClient = new HelmChartClient(new DefaultKubernetesClient());

        // queue for helm chart action events.
        BlockingQueue<HelmChartActionEvent> queue = new LinkedBlockingQueue<>(10);

        // action handler.
        ActionHandler<HelmChart> actionHandler = new HelmChartActionHandler();

        // start queue consumer.
        new Thread(new HelmChartQueueConsumer(queue, actionHandler)).start();
        LOG.info("helm chart queue consumer started...");

        // start helm chart watcher.
        new Thread(new HelmChartWatchRunnable(helmChartClient, queue)).start();
        LOG.info("helm chart watcher started...");

        LOG.info("helm operator is running now...");
        while(true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
