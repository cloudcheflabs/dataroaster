package com.cloudcheflabs.dataroaster.operators.helm.handler;

import com.cloudcheflabs.dataroaster.operators.helm.crd.HelmChart;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class HelmChartWatcher implements Watcher<HelmChart>{
    private static Logger LOG = LoggerFactory.getLogger(HelmChartWatcher.class);

    private BlockingQueue<HelmChartActionEvent> queue;

    public HelmChartWatcher(BlockingQueue<HelmChartActionEvent> queue) {
        this.queue = queue;
    }

    @Override
    public void eventReceived(Watcher.Action action, HelmChart helmChart) {
        LOG.info("event received - action: {}, name: {}", action.name(), helmChart.getMetadata().getName());
        try {
            queue.put(new HelmChartActionEvent(action, helmChart));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(WatcherException e) {
        LOG.error("watcher exception: " + e.getMessage());
        e.printStackTrace();
        //throw new RuntimeException(e);
        System.exit(1);
    }
}
