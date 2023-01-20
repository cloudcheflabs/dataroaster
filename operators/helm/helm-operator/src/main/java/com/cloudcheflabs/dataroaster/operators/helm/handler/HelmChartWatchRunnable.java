package com.cloudcheflabs.dataroaster.operators.helm.handler;

import com.cloudcheflabs.dataroaster.operators.helm.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.operators.helm.crd.HelmChart;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class HelmChartWatchRunnable implements Runnable{
    private static Logger LOG = LoggerFactory.getLogger(HelmChartWatchRunnable.class);

    private BlockingQueue<HelmChartActionEvent> queue;
    private MixedOperation<HelmChart, KubernetesResourceList<HelmChart>, Resource<HelmChart>> helmChartClient;

    public HelmChartWatchRunnable(BlockingQueue<HelmChartActionEvent> queue) {
        helmChartClient = SpringContextSingleton.getInstance().getBean(HelmChartClient.class).getHelmChartClient();
        this.queue = queue;
    }

    @Override
    public void run() {
        int watchCloseCount = 0;
        while(true) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            try {
                Watcher watcher = new HelmChartWatcher(queue);
                helmChartClient.watch(watcher);
                LOG.info("Watch helm charts...");
                countDownLatch.await();
            } catch (Exception e) {
                LOG.error("exception occurred: {}", e.getMessage());
                e.printStackTrace();
                countDownLatch.countDown();
                LOG.error("watch close count: {}", ++watchCloseCount);
                try {
                    Thread.sleep(5000);
                    LOG.info("trying to watch custom resource again...");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }
}
