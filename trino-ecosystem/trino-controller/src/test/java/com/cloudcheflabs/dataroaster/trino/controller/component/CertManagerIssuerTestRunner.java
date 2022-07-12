package com.cloudcheflabs.dataroaster.trino.controller.component;

import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import com.cloudcheflabs.dataroaster.trino.controller.util.YamlUtils;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
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

public class CertManagerIssuerTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(CertManagerIssuerTestRunner.class);

    private KubernetesClient kubernetesClient;


    @Before
    public void setup() throws Exception {
        kubernetesClient = SpringContextSingleton.getInstance().getBean(KubernetesClient.class);
    }


    @Test
    public void createIssuer() throws Exception {
        String kubeconfigPath = System.getProperty(PROPERTY_TRINO_CONTROLLER_KUBECONFIG);
        if(kubeconfigPath == null) {
            System.out.printf("[%s] system property needs to be provided.\n", PROPERTY_TRINO_CONTROLLER_KUBECONFIG);
            return;
        }

        Map<String, Object> kv = new HashMap<>();
        String yaml =
                TemplateUtils.replace("/templates/cr/prod-issuer.yaml", true, kv);

        String kind = "ClusterIssuer";

        CustomResourceDefinition selectedCRD = selectCRD(kind);
        if(selectedCRD == null) {
            throw new IllegalStateException("CRD with kind [" + kind + "] not found!");
        }

        LOG.info("crd: \n{}", YamlUtils.objectToYaml(selectedCRD));

        InputStream is = new ByteArrayInputStream(yaml.getBytes());

        GenericKubernetesResource genericKubernetesResource =
                kubernetesClient.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(selectedCRD))
                        .load(is).get();


        GenericKubernetesResource retResource =
                kubernetesClient.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(selectedCRD))
                        .createOrReplace(genericKubernetesResource);

    }

    private CustomResourceDefinition selectCRD(String kind) {
        CustomResourceDefinitionList crds = kubernetesClient.apiextensions().v1().customResourceDefinitions().list();
        List<CustomResourceDefinition> crdsItems = crds.getItems();
        LOG.info("[{}] CRDs found", crdsItems.size());

        CustomResourceDefinition selectedCRD = null;
        for (CustomResourceDefinition crd : crdsItems) {
            CustomResourceDefinitionSpec spec = crd.getSpec();
            String group = spec.getGroup();
            String crdKind = spec.getNames().getKind();
            LOG.info("group: [{}], crdKind: [{}]", group, crdKind);

            if(group.contains("cloudchef-labs.com") && crdKind.equals(kind)) {
                selectedCRD = crd;
                break;
            }
        }

        // if there is no crd selected, search for crd outside group of cloudchef-labs.com.
        if(selectedCRD == null) {
            for (CustomResourceDefinition crd : crdsItems) {
                CustomResourceDefinitionSpec spec = crd.getSpec();
                String group = spec.getGroup();
                String crdKind = spec.getNames().getKind();
                LOG.info("group: [{}], crdKind: [{}]", group, crdKind);

                if(crdKind.equals(kind)) {
                    selectedCRD = crd;
                    break;
                }
            }
        }

        return selectedCRD;
    }

}
