package com.cloudcheflabs.dataroaster.operators.trino.handler;

import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class TrinoClusterWatcher implements Watcher<TrinoCluster>{
    private static Logger LOG = LoggerFactory.getLogger(TrinoClusterWatcher.class);

    private BlockingQueue<TrinoClusterActionEvent> queue;

    public TrinoClusterWatcher(BlockingQueue<TrinoClusterActionEvent> queue) {
        this.queue = queue;
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
        LOG.error("watcher exception: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException(e);
    }
}
