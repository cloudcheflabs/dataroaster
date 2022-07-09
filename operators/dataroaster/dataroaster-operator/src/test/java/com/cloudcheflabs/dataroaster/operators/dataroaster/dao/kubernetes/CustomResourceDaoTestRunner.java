package com.cloudcheflabs.dataroaster.operators.dataroaster.dao.kubernetes;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.CustomResourceDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;
import com.cloudcheflabs.dataroaster.operators.dataroaster.test.SpringBootTestRunnerBase;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomResourceDaoTestRunner extends SpringBootTestRunnerBase {

    private static Logger LOG = LoggerFactory.getLogger(CustomResourceDaoTestRunner.class);

    private static CustomResourceDao customResourceDao;
    private static KubernetesClient kubernetesClient;

    @BeforeClass
    public static void setup() throws Exception {
        init();
        customResourceDao = applicationContext.getBean("hibernateCustomResourceDao", CustomResourceDao.class);
        kubernetesClient = applicationContext.getBean(KubernetesClient.class);
    }

    @Test
    public void select() throws Exception {

        CustomResource customResource =
                customResourceDao.findCustomResource("trino-cluster-etl", "trino-operator", "TrinoCluster");
        Assert.assertNotNull(customResource);
        LOG.info("cr: [{}]", JsonUtils.toJson(customResource));
    }

}
