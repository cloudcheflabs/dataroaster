package com.cloudcheflabs.dataroaster.operators.helm.handler;

import com.cloudcheflabs.dataroaster.operators.helm.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.operators.helm.crd.HelmChart;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
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
        try {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            helmChartClient.watch(new HelmChartWatcher(queue, countDownLatch, this));
            LOG.info("Watch helm charts...");
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
