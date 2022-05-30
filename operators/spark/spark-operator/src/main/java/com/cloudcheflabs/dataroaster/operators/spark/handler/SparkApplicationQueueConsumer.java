package com.cloudcheflabs.dataroaster.operators.spark.handler;

import com.cloudcheflabs.dataroaster.operators.spark.crd.SparkApplication;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class SparkApplicationQueueConsumer implements Runnable {

    private static Logger LOG = LoggerFactory.getLogger(SparkApplicationQueueConsumer.class);

    private BlockingQueue<SparkApplicationActionEvent> queue;
    private ActionHandler actionHandler;

    public SparkApplicationQueueConsumer(BlockingQueue<SparkApplicationActionEvent> queue, ActionHandler actionHandler) {
        this.queue = queue;
        this.actionHandler = actionHandler;
    }

    @Override
    public void run() {
        while (true) {
            try {
                SparkApplicationActionEvent sparkApplicationActionEvent = queue.take();
                if(sparkApplicationActionEvent != null) {
                    Watcher.Action action = sparkApplicationActionEvent.getAction();
                    SparkApplication sparkApplication = sparkApplicationActionEvent.getSparkApplication();
                    if(action.name().equals("ADDED")) {
                        LOG.info("new spark application: \n{}", sparkApplication.toString());
                        actionHandler.submit(sparkApplication);
                        LOG.info("[{}] submitted...", sparkApplication.getMetadata().getName());
                    } else if(action.name().equals("DELETED")) {
                        actionHandler.destroy(sparkApplication);
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
