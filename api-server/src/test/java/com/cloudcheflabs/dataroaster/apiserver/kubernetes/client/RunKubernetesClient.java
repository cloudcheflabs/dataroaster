package com.cloudcheflabs.dataroaster.apiserver.kubernetes.client;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:application-test.properties"
})
public class RunKubernetesClient {

    private static Logger LOG = LoggerFactory.getLogger(RunKubernetesClient.class);

    @Test
    public void accessKubernetesWithKubeconfig() throws Exception
    {
        String kubeconfigPath = "/home/opc/.kube/config";

        String rawKubeconfig = null;
        FileInputStream inputStream = new FileInputStream(kubeconfigPath);
        try {
            rawKubeconfig = IOUtils.toString(inputStream);
        } finally {
            inputStream.close();
        }

        LOG.info("rawKubeconfig read from file: \n{}", rawKubeconfig);

        Config config = Config.fromKubeconfig(rawKubeconfig);

        try (KubernetesClient client = new DefaultKubernetesClient(config)) {
            client.storage().storageClasses().list().getItems()
                    .forEach(sc -> LOG.info("Storage class: {}", sc.getMetadata().getName()));
        } catch (KubernetesClientException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
