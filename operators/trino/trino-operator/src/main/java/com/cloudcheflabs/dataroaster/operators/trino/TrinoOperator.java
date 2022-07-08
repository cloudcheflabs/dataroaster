package com.cloudcheflabs.dataroaster.operators.trino;

import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import com.cloudcheflabs.dataroaster.operators.trino.handler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrinoOperator implements Runnable, InitializingBean {
    private static Logger LOG = LoggerFactory.getLogger(TrinoOperator.class);

    private ActionHandler<TrinoCluster> actionHandler;

    private TrinoClusterClient trinoClusterClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread t = new Thread(this);
        t.start();
    }

    public TrinoOperator(TrinoClusterClient trinoClusterClient, ActionHandler<TrinoCluster> actionHandler) {
        this.trinoClusterClient = trinoClusterClient;
        this.actionHandler = actionHandler;
    }

    @Override
    public void run() {

        // queue for trino cluster action events.
        BlockingQueue<TrinoClusterActionEvent> queue = new LinkedBlockingQueue<>(10);

        // start queue consumer.
        new Thread(new TrinoClusterQueueConsumer(queue, actionHandler)).start();
        LOG.info("trino cluster queue consumer started...");

        // start trino cluster watcher.
        new Thread(new TrinoClusterWatchRunnable(trinoClusterClient, queue)).start();
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
