package com.cloudcheflabs.dataroaster.operators.trino.handler;

import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import com.cloudcheflabs.dataroaster.operators.trino.util.YamlUtils;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class TrinoClusterQueueConsumer implements Runnable{

    private static Logger LOG = LoggerFactory.getLogger(TrinoClusterQueueConsumer.class);

    private BlockingQueue<TrinoClusterActionEvent> queue;
    private ActionHandler<TrinoCluster> actionHandler;

    public TrinoClusterQueueConsumer(BlockingQueue<TrinoClusterActionEvent> queue, ActionHandler<TrinoCluster> actionHandler) {
        this.queue = queue;
        this.actionHandler = actionHandler;
    }

    @Override
    public void run() {
        while (true) {
            try {
                TrinoClusterActionEvent actionEvent = queue.take();
                if(actionEvent != null) {
                    Watcher.Action action = actionEvent.getAction();
                    TrinoCluster trinoCluster = actionEvent.getTrinoCluster();
                    if(action.name().equals("ADDED")) {
                        LOG.info("add trino cluster: \n{}", YamlUtils.objectToYaml(trinoCluster));
                        actionHandler.create(trinoCluster);
                        LOG.info("[{}] created...", trinoCluster.getMetadata().getName());
                    } else if(action.name().equals("DELETED")) {
                        LOG.info("delete trino cluster: \n{}", YamlUtils.objectToYaml(trinoCluster));
                        actionHandler.destroy(trinoCluster);
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
