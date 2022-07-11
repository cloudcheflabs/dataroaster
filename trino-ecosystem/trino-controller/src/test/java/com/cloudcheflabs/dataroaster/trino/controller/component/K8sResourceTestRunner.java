package com.cloudcheflabs.dataroaster.trino.controller.component;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import com.cloudcheflabs.dataroaster.trino.controller.util.YamlUtils;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cloudcheflabs.dataroaster.trino.controller.config.KubernetesClientConfigurer.PROPERTY_TRINO_CONTROLLER_KUBECONFIG;

public class K8sResourceTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(K8sResourceTestRunner.class);

    private KubernetesClient kubernetesClient;


    @Before
    public void setup() throws Exception {
        kubernetesClient = SpringContextSingleton.getInstance().getBean(KubernetesClient.class);
    }


    @Test
    public void getExternalIP() throws Exception {
        String kubeconfigPath = System.getProperty(PROPERTY_TRINO_CONTROLLER_KUBECONFIG);
        if(kubeconfigPath == null) {
            System.out.printf("[%s] system property needs to be provided.\n", PROPERTY_TRINO_CONTROLLER_KUBECONFIG);
            return;
        }

        Service nginxService = kubernetesClient.services().inNamespace("ingress-nginx").withName("ingress-nginx-controller").get();
        List<String> externalIPs = nginxService.getSpec().getExternalIPs();
        LOG.info("external ips: {}", JsonUtils.toJson(externalIPs));
    }

}
