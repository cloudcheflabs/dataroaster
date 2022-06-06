package com.cloudcheflabs.dataroaster.operators.trino.handler;

import com.cloudcheflabs.dataroaster.operators.trino.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class TrinoClusterWatchRunnable implements Runnable{
    private static Logger LOG = LoggerFactory.getLogger(TrinoClusterWatchRunnable.class);

    private MixedOperation<TrinoCluster, KubernetesResourceList<TrinoCluster>, Resource<TrinoCluster>> trinoClusterClient;
    private BlockingQueue<TrinoClusterActionEvent> queue;

    public TrinoClusterWatchRunnable(BlockingQueue<TrinoClusterActionEvent> queue) {
        this.trinoClusterClient =
                SpringContextSingleton.getInstance().getBean(TrinoClusterClient.class).getTrinoClusterClient();;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            trinoClusterClient.watch(new TrinoClusterWatcher(queue, countDownLatch));
            LOG.info("Watch trino clusters...");
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
