package com.cloudcheflabs.dataroaster.operators.trino;

import com.cloudcheflabs.dataroaster.operators.trino.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import com.cloudcheflabs.dataroaster.operators.trino.handler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrinoOperator {
    private static Logger LOG = LoggerFactory.getLogger(TrinoOperator.class);

    public static void main(String[] args) {

        // load spring application context.
        SpringContextSingleton.getInstance();
        LOG.info("spring application context loaded...");

        // queue for trino cluster action events.
        BlockingQueue<TrinoClusterActionEvent> queue = new LinkedBlockingQueue<>(10);

        // action handler.
        ActionHandler<TrinoCluster> actionHandler = new TrinoClusterActionHandler();

        // start queue consumer.
        new Thread(new TrinoClusterQueueConsumer(queue, actionHandler)).start();
        LOG.info("trino cluster queue consumer started...");

        // start trino cluster watcher.
        new Thread(new TrinoClusterWatchRunnable(queue)).start();
        LOG.info("trino cluster watcher started...");

        LOG.info("trino operator is running now...");
        while(true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
