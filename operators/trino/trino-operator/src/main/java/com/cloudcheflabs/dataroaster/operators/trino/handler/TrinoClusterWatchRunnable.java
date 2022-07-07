package com.cloudcheflabs.dataroaster.operators.trino.handler;

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

    private MixedOperation<TrinoCluster, KubernetesResourceList<TrinoCluster>, Resource<TrinoCluster>> trinoClusterClientMixedOperation;
    private BlockingQueue<TrinoClusterActionEvent> queue;

    public TrinoClusterWatchRunnable(TrinoClusterClient trinoClusterClient, BlockingQueue<TrinoClusterActionEvent> queue) {
        this.trinoClusterClientMixedOperation = trinoClusterClient.getTrinoClusterClient();;
        this.queue = queue;
    }

    @Override
    public void run() {
        while(true) {
            try {
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                trinoClusterClientMixedOperation.watch(new TrinoClusterWatcher(queue, countDownLatch));
                LOG.info("Watch trino clusters...");
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                LOG.error("watch error", e);
            }
        }
    }
}
