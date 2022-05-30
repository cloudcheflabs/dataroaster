package com.cloudcheflabs.dataroaster.operators.spark.handler;

import com.cloudcheflabs.dataroaster.operators.spark.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.operators.spark.crd.SparkApplication;
import com.cloudcheflabs.dataroaster.operators.spark.crd.SparkApplicationList;
import com.cloudcheflabs.dataroaster.operators.spark.util.SparkApplicationExecutor;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SparkApplicationActionHandler implements ActionHandler{

    private static Logger LOG = LoggerFactory.getLogger(SparkApplicationActionHandler.class);

    private NonNamespaceOperation<SparkApplication, SparkApplicationList, Resource<SparkApplication>> sparkApplicationClient;

    public SparkApplicationActionHandler() {
        this.sparkApplicationClient =
                SpringContextSingleton.getInstance().getBean(SparkApplicationClient.class).getSparkApplicationClient();;
    }

    @Override
    public void submit(SparkApplication sparkApplication) {
        // run spark application.
        SparkApplicationExecutor.runTask(() -> {
            return SparkSubmitHandler.runApplication(sparkApplication);
        });
    }

    @Override
    public void destroy(SparkApplication sparkApplication) {
        // delete resources in kubernetes cluster.
        KubernetesClient client = SpringContextSingleton.getInstance().getBean(KubernetesClient.class);
        String namespace = sparkApplication.getSpec().getCore().getNamespace();
        int MAX = 60;
        int count = 0;
        while(true) {
            PodList podList = client.pods().inNamespace(namespace).list();
            if (podList != null) {
                for (Pod pod : podList.getItems()) {
                    String podName = pod.getMetadata().getName();
                    String sparkApplicationName = sparkApplication.getMetadata().getName();
                    if (podName.startsWith(sparkApplicationName)) {
                        Map<String, String> labels = pod.getMetadata().getLabels();
                        if (labels.containsKey("spark-role")) {
                            String sparkRole = labels.get("spark-role");
                            if (sparkRole.equals("driver")) {
                                // delete spark driver pod.
                                LOG.info("spark application [{}] will be deleted...", sparkApplicationName);
                                client.pods().inNamespace(namespace).withName(podName).delete();
                                LOG.info("spark application [{}] deleted from cluster", sparkApplication.getMetadata().getName());
                                return;
                            }
                        }
                    }
                }
            }
            try {
                Thread.sleep(1000);
                count++;
                if(count == MAX) {
                    LOG.warn("spark application [{}] not found in cluster", sparkApplication.getMetadata().getName());
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
