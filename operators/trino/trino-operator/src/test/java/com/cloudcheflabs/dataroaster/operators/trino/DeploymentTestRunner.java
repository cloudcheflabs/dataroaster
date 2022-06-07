package com.cloudcheflabs.dataroaster.operators.trino;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import com.cloudcheflabs.dataroaster.operators.trino.util.YamlUtils;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@EnableKubernetesMockClient(crud = true)
public class DeploymentTestRunner {

    KubernetesClient client;

    @Test
    public void replaceYamlWithJinja() throws Exception {
        String deployYaml = loadYaml();
        System.out.printf("deployYaml: \n%s", deployYaml);
    }

    @Test
    public void crud() throws Exception {
        String deployYaml = loadYaml();
        InputStream is = new ByteArrayInputStream(deployYaml.getBytes());
        Deployment deployment = client.apps().deployments().load(is).get();
        System.out.printf("deployment loaded: \n%s", YamlUtils.objectToYaml(deployment));

        // create deployment.
        client.apps().deployments().inNamespace("test").create(deployment);

        DeploymentList deploymentList = client.apps().deployments().inNamespace("test").list();
        Assert.assertEquals(1, deploymentList.getItems().size());

        Deployment retDeployment = client.apps().deployments().inNamespace("test").withName("nginx").get();
        Assert.assertNull(retDeployment.getStatus());

        DeploymentStatus deploymentStatus = new DeploymentStatus();
        deploymentStatus.setReplicas(2);
        deployment.setStatus(deploymentStatus);

        // update deployment.
        retDeployment = client.apps().deployments().inNamespace("test").withName("nginx").patch(deployment);
        Assert.assertNotNull(retDeployment.getStatus());
        Assert.assertEquals(2, retDeployment.getStatus().getReplicas().intValue());

        // delete deployment.
        boolean deleted = client.apps().deployments().inNamespace("test").withName("nginx").delete();
        Assert.assertTrue(deleted);
    }

    private String loadYaml() {
        InputStream is = FileUtils.readFileFromClasspath("/k8s-manifests/deploy-nginx.yaml");
        Map<String, String> kv = new HashMap<>();
        kv.put("port", "8080");

        return TemplateUtils.replace("/k8s-manifests/deploy-nginx.yaml", true, kv);
    }
}
