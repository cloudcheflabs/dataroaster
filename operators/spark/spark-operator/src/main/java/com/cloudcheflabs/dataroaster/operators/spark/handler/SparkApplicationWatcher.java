package com.cloudcheflabs.dataroaster.operators.spark.handler;

import com.cloudcheflabs.dataroaster.operators.spark.crd.SparkApplication;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class SparkApplicationWatcher implements Watcher<SparkApplication> {

    private static Logger LOG = LoggerFactory.getLogger(SparkApplicationWatcher.class);

    private BlockingQueue<SparkApplicationActionEvent> queue;

    public SparkApplicationWatcher(BlockingQueue<SparkApplicationActionEvent> queue) {
        this.queue = queue;
    }

    @Override
    public void eventReceived(Action action, SparkApplication sparkApplication) {
        LOG.info("action: {}, name: {}", action.name(), sparkApplication.getMetadata().getName());
        try {
            queue.put(new SparkApplicationActionEvent(action, sparkApplication));
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
