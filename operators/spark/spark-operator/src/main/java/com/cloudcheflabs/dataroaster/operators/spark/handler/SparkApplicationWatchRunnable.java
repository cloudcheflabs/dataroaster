package com.cloudcheflabs.dataroaster.operators.spark.handler;

import com.cloudcheflabs.dataroaster.operators.spark.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.operators.spark.crd.SparkApplication;
import com.cloudcheflabs.dataroaster.operators.spark.crd.SparkApplicationList;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class SparkApplicationWatchRunnable implements Runnable {

    private static Logger LOG = LoggerFactory.getLogger(SparkApplicationWatchRunnable.class);

    private NonNamespaceOperation<SparkApplication, SparkApplicationList, Resource<SparkApplication>> sparkApplicationClient;
    private BlockingQueue<SparkApplicationActionEvent> queue;

    public SparkApplicationWatchRunnable(BlockingQueue<SparkApplicationActionEvent> queue) {
        this.sparkApplicationClient =
                SpringContextSingleton.getInstance().getBean(SparkApplicationClient.class).getSparkApplicationClient();;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            sparkApplicationClient.watch(new SparkApplicationWatcher(queue, countDownLatch));
            LOG.info("Watch spark applications...");
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
