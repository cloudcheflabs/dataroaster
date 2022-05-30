package com.cloudcheflabs.dataroaster.operators.spark;


import com.cloudcheflabs.dataroaster.operators.spark.api.dao.ResourceDao;
import com.cloudcheflabs.dataroaster.operators.spark.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.operators.spark.handler.*;
import com.cloudcheflabs.dataroaster.operators.spark.util.HttpUtils;
import com.cloudcheflabs.dataroaster.operators.spark.util.S3Utils;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class SparkOperatorTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(SparkOperatorTestRunner.class);

    @Test
    public void runMain() throws Exception {
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



    @Test
    public void getSecret() throws Exception {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ResourceDao resourceDao = applicationContext.getBean(ResourceDao.class);

        // get access key from secret map.
        Map<String, String> accessKeySecretMap = resourceDao.getSecret("spark-operator", "s3-secret");
        String accessKeyEncoded = accessKeySecretMap.get("accessKey");
        LOG.info("accessKeyEncoded: {}", accessKeyEncoded);

        String accessKeyDecoded = new String(Base64.getDecoder().decode(accessKeyEncoded));
        LOG.info("accessKeyDecoded: {}", accessKeyDecoded);

        Map<String, String> secretKeySecretMap = resourceDao.getSecret("spark-operator", "s3-secret");
        String secretKeyEncoded = secretKeySecretMap.get("secretKey");
        LOG.info("secretKeyEncoded: {}", secretKeyEncoded);

        String secretKeyDecoded = new String(Base64.getDecoder().decode(secretKeyEncoded));
        LOG.info("secretKeyDecoded: {}", secretKeyDecoded);
    }

    @Test
    public void downloadObjectFromS3() throws Exception {
        String applicationFileUrl = "s3a://mykidong/spark-app/pi.py";

        S3Utils.downloadObject("48d4ec0c682cec9f1bc97ffc563c27827651b07b",
                "akOGv1TgyV/p0RZEQFFCkzhHGsxpbcI9AQfPmeXeR8U=",
                "https://cnobgk2u8blu.compat.objectstorage.ap-seoul-1.oraclecloud.com",
                applicationFileUrl,
                System.getProperty("user.home") + "/pi-downloaded.py");
    }

    @Test
    public void tokenize() throws Exception {
        String applicationFileUrl = "s3a://mykidong/spark-app/spark-thrift-server-3.0.3-spark-job.jar";

        applicationFileUrl = applicationFileUrl.replaceAll("s3a://", "");
        int index = applicationFileUrl.indexOf("/");
        String s3Bucket = applicationFileUrl.substring(0, index);
        String objectPath = applicationFileUrl.substring(index + 1, applicationFileUrl.length());

        LOG.info("s3 bucket: [{}], object path: [{}]", s3Bucket, objectPath);
    }

    @Test
    public void getLastTokenFromString() throws Exception {
        String applicationFileUrl = "s3a://mykidong/spark-app/spark-thrift-server-3.0.3-spark-job.jar";
        int index = applicationFileUrl.lastIndexOf("/");
        String fileName = applicationFileUrl.substring(index + 1, applicationFileUrl.length());
        LOG.info("file name: [{}]", fileName);
    }

    @Test
    public void downloadFileFromUrl() throws Exception {
        String applicationFileUrl = "https://github.com/cloudcheflabs/spark/releases/download/v3.0.3/spark-thrift-server-3.0.3-spark-job.jar";
        HttpUtils.downloadFileFromHttpRest(applicationFileUrl, System.getProperty("user.home") + "/download-from-http.jar");
    }

    @Test
    public void watchPodStatus() throws Exception {
        KubernetesClient client = SpringContextSingleton.getInstance().getBean(KubernetesClient.class);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        client.pods().inNamespace("spark").watch(new Watcher<Pod>() {
            @Override
            public void eventReceived(Action action, Pod pod) {
                String podName = pod.getMetadata().getName();
                if(podName.startsWith("spark-thrift-server-minimal")) {
                    String phase = pod.getStatus().getPhase();
                    LOG.info("pod: [{}], phase: [{}]", podName, phase);
                    if(phase.equals("Running")) {
                        countDownLatch.countDown();
                    }
                }
            }

            @Override
            public void onClose(WatcherException e) {

            }
        });
        countDownLatch.await();
    }
}
