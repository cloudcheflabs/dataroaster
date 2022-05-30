package com.cloudcheflabs.dataroaster.operators.spark;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import com.cloudcheflabs.dataroaster.operators.spark.handler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SparkOperator {

    private static Logger LOG = LoggerFactory.getLogger(SparkOperator.class);

    public static void main(String[] args) {

        // create kubeconfig file.

        // read service account token file.
        String tokenFile = "/var/run/secrets/kubernetes.io/serviceaccount/token";
        String token = FileUtils.fileToString(tokenFile, false);
        //LOG.info("token: {}", token);

        // kubeconfig file path.
        String kubeconfigFilePath = System.getProperty("user.home") + "/.kube/config";

        // replace token param with token value and save kubeconfig file.
        Map<String, Object> kv = new HashMap<>();
        kv.put("token", token);
        TemplateUtils.toFile("/templates/kubeconfig/config", true, kv, kubeconfigFilePath, false);

        // queue for new spark applications.
        BlockingQueue<SparkApplicationActionEvent> queue = new LinkedBlockingQueue<>(10);

        // action handler.
        ActionHandler actionHandler = new SparkApplicationActionHandler();

        // start queue consumer.
        new Thread(new SparkApplicationQueueConsumer(queue, actionHandler)).start();

        // thread of spark application watcher.
        new Thread(new SparkApplicationWatchRunnable(queue)).start();

        LOG.info("spark operator is running now...");
        while(true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
