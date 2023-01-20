package com.cloudcheflabs.dataroaster.operators.trino.handler;

import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.Watcher;
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
        int watchCloseCount = 0;
        while(true) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            try {
                Watcher watcher = new TrinoClusterWatcher(queue);
                trinoClusterClientMixedOperation.watch(watcher);
                LOG.info("Watch trino clusters...");
                countDownLatch.await();
            } catch (Exception e) {
                LOG.error("exception occurred: {}", e.getMessage());
                e.printStackTrace();
                countDownLatch.countDown();
                LOG.error("watch close count: {}", ++watchCloseCount);
                try {
                    Thread.sleep(5000);
                    LOG.info("trying to watch custom resource again...");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }
}
