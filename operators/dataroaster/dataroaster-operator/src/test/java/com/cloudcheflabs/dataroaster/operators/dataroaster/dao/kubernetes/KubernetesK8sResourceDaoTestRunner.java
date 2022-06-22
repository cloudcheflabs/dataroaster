package com.cloudcheflabs.dataroaster.operators.dataroaster.dao.kubernetes;

import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.K8sResourceDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.test.SpringBootTestRunnerBase;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KubernetesK8sResourceDaoTestRunner extends SpringBootTestRunnerBase {

    private static Logger LOG = LoggerFactory.getLogger(KubernetesK8sResourceDaoTestRunner.class);

    private static K8sResourceDao k8sResourceDao;
    private static KubernetesClient kubernetesClient;

    @BeforeClass
    public static void setup() throws Exception {
        init();
        k8sResourceDao = applicationContext.getBean("kubernetesK8sResourceDao", K8sResourceDao.class);
        kubernetesClient = applicationContext.getBean(KubernetesClient.class);
    }

    @Test
    public void listCRD() throws Exception {
        // system property -DdataroasterKubeconfig=... must be set before running test.

        CustomResourceDefinitionList crds = kubernetesClient.apiextensions().v1().customResourceDefinitions().list();
        List<CustomResourceDefinition> crdsItems = crds.getItems();
        LOG.info("[{}] CRDs found", crdsItems.size());

        CustomResourceDefinition selectedCRD = null;
        for (CustomResourceDefinition crd : crdsItems) {
            CustomResourceDefinitionSpec spec = crd.getSpec();
            String group = spec.getGroup();
            String kind = spec.getNames().getKind();
            LOG.info("group: [{}], kind: [{}]", group, kind);

            if(group.contains("cloudchef-labs.com") && kind.equals("")) {
                selectedCRD = crd;
                break;
            }
        }
        if(selectedCRD == null) {
            // CRD not found exception thrown!
        }
    }
}
