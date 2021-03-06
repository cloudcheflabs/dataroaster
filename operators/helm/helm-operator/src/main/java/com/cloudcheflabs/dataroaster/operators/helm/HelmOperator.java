package com.cloudcheflabs.dataroaster.operators.helm;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import com.cloudcheflabs.dataroaster.operators.helm.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.operators.helm.crd.HelmChart;
import com.cloudcheflabs.dataroaster.operators.helm.handler.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HelmOperator {
    private static Logger LOG = LoggerFactory.getLogger(HelmOperator.class);

    public static void main(String[] args) {

        // initialize default kubernetes client before creating kubeconfig file.
        SpringContextSingleton.getInstance().getBean(KubernetesClient.class);

        // create kubeconfig file.

        // read service account token file.
        String tokenFile = "/var/run/secrets/kubernetes.io/serviceaccount/token";
        String token = FileUtils.fileToString(tokenFile, false);
        //LOG.info("token: {}", token);

        // kubeconfig file path.
        String kubeConfigDir = System.getProperty("user.home") + "/.kube";
        FileUtils.createDirectory(kubeConfigDir);
        String kubeconfigFilePath = kubeConfigDir + "/config";

        // replace token param with token value and save kubeconfig file.
        Map<String, Object> kv = new HashMap<>();
        kv.put("token", token);
        TemplateUtils.toFile("/templates/kubeconfig/config", true, kv, kubeconfigFilePath, false);

        // kubeconfig.
        //LOG.info("kubeconfig: \n{}", com.cloudcheflabs.dataroaster.common.util.FileUtils.fileToString(kubeconfigFilePath, false));

        // queue for helm chart action events.
        BlockingQueue<HelmChartActionEvent> queue = new LinkedBlockingQueue<>(10);

        // action handler.
        ActionHandler<HelmChart> actionHandler = new HelmChartActionHandler();

        // start queue consumer.
        new Thread(new HelmChartQueueConsumer(queue, actionHandler)).start();
        LOG.info("helm chart queue consumer started...");

        // start helm chart watcher.
        new Thread(new HelmChartWatchRunnable(queue)).start();
        LOG.info("helm chart watcher started...");

        LOG.info("helm operator is running now...");
        while(true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
