package com.cloudcheflabs.dataroaster.trino.controller.component;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import com.cloudcheflabs.dataroaster.trino.controller.api.service.K8sResourceService;
import com.cloudcheflabs.dataroaster.trino.controller.domain.CustomResource;
import com.cloudcheflabs.dataroaster.trino.controller.util.ContainerStatusChecker;
import com.cloudcheflabs.dataroaster.trino.controller.util.CustomResourceUtils;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Initializer {

    private static Logger LOG = LoggerFactory.getLogger(Initializer.class);

    public static final String DEFAULT_TRINO_CONTROLLER_NAMESPACE = "trino-controller";

    @Autowired
    @Qualifier("kubernetesClient")
    private KubernetesClient kubernetesClient;

    @Autowired
    @Qualifier("k8sResourceServiceImpl")
    private K8sResourceService k8sResourceService;


    public Initializer() {
    }

    public void init() {
        LOG.info("ready to run initializer...");

        // replace nginx templates.
        Map<String, Object> kv = new HashMap<>();
        kv.put("customResourceNamespace", getNamespace());
        String nginxCrString =
                TemplateUtils.replace("/templates/cr/nginx-ingress-controller.yaml", true, kv);

        // nginx namespace.
        String nginxNamespace = CustomResourceUtils.getTargetNamespace(nginxCrString);

        // check if nginx is running.
        boolean nginxRunning = ContainerStatusChecker.isRunning(
                kubernetesClient,
                "ingress-nginx-controller",
                nginxNamespace,
                "app.kubernetes.io/component",
                "controller"
                );
        LOG.info("nginx is running: {}", nginxRunning);

        // if not, install nginx.
        if(!nginxRunning) {
            // build custom resource of nginx.
            CustomResource nginxCr = CustomResourceUtils.fromYaml(nginxCrString);

            // create cr of nginx.
            k8sResourceService.createCustomResource(nginxCr);
            LOG.info("nginx custom resource created...");

            // wait for that nginx will be run.
            ContainerStatusChecker.checkContainerStatus(
                    kubernetesClient,
                    "ingress-nginx-controller",
                    nginxNamespace,
                    "app.kubernetes.io/component",
                    "controller",
                    20
            );
        }


        // replace cert manager template.
        kv = new HashMap<>();
        kv.put("customResourceNamespace", getNamespace());
        String certManagerString =
                TemplateUtils.replace("/templates/cr/cert-manager.yaml", true, kv);


        // cert manager namespace.
        String certManagerNamespace = CustomResourceUtils.getTargetNamespace(certManagerString);

        // check if cert manager is running.
        boolean certManagerRunning = ContainerStatusChecker.isRunning(
                kubernetesClient,
                "cert-manager",
                certManagerNamespace,
                "app.kubernetes.io/component",
                "controller"
        );
        LOG.info("cert manager is running: {}", certManagerRunning);

        // if not, install cert manager.
        if(!certManagerRunning) {
            // build custom resource of cert manager.
            CustomResource certManagerCr = CustomResourceUtils.fromYaml(certManagerString);

            // create cr of cert manager.
            k8sResourceService.createCustomResource(certManagerCr);
            LOG.info("cert manager custom resource created...");

            // wait for that cert manager will be run.
            ContainerStatusChecker.checkContainerStatus(
                    kubernetesClient,
                    "cert-manager",
                    certManagerNamespace,
                    "app.kubernetes.io/component",
                    "controller",
                    20
            );

            // take a time to wait for cert manager being loaded.
            LOG.info("take a time to wait for cert manager being loaded...");
            try {
                Thread.sleep(20000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // create issuer.
            kv = new HashMap<>();
            String issuerString =
                    TemplateUtils.replace("/templates/cr/prod-issuer.yaml", true, kv);
            // build custom resource of issuer.
            CustomResource issuerCr = CustomResourceUtils.fromYaml(issuerString);
            k8sResourceService.createCustomResource(issuerCr);
            LOG.info("issuer custom resource created...");
        }

        // TODO: get external ip of nginx service.
        Service nginxService = kubernetesClient.services().inNamespace(nginxNamespace).withName("ingress-nginx-controller").get();
        List<String> externalIPs = nginxService.getSpec().getExternalIPs();

        // TODO: get public endpoint of trino gateway.

        // TODO: add dns record to public dns, for instance, name.com

        // TODO: install trino gateway.
    }

    public static String getNamespace() {
        try {
            String namespaceFile = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";
            return FileUtils.fileToString(namespaceFile, false);
        } catch (Exception e) {
            System.out.printf("instead return default namespace [%s]\n", DEFAULT_TRINO_CONTROLLER_NAMESPACE);
            return DEFAULT_TRINO_CONTROLLER_NAMESPACE;
        }
    }

}
