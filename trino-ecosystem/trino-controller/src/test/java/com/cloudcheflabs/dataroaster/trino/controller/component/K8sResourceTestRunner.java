package com.cloudcheflabs.dataroaster.trino.controller.component;

import io.fabric8.kubernetes.api.model.LoadBalancerIngress;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
        LoadBalancerIngress loadBalancerIngress = nginxService.getStatus().getLoadBalancer().getIngress().get(0);
        String ip = loadBalancerIngress.getIp();
        if(ip == null) {
            String hostName = loadBalancerIngress.getHostname();

            // get ip address of host name.
            try {
                InetAddress host = InetAddress.getByName(hostName);
                ip = host.getHostAddress();
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            }
        }

        LOG.info("external ip: {}", ip);
    }

}
