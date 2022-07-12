package com.cloudcheflabs.dataroaster.operators.trino.handler;

import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class TrinoClusterWatcher implements Watcher<TrinoCluster>{
    private static Logger LOG = LoggerFactory.getLogger(TrinoClusterWatcher.class);

    private final CountDownLatch countDownLatch;
    private BlockingQueue<TrinoClusterActionEvent> queue;

    public TrinoClusterWatcher(BlockingQueue<TrinoClusterActionEvent> queue, CountDownLatch countDownLatch) {
        this.queue = queue;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void eventReceived(Watcher.Action action, TrinoCluster trinoCluster) {
        LOG.info("event received - action: {}, name: {}", action.name(), trinoCluster.getMetadata().getName());
        try {
            queue.put(new TrinoClusterActionEvent(action, trinoCluster));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(WatcherException e) {
        this.countDownLatch.countDown();
        LOG.info("close watcher");
    }
}
