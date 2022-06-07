package com.cloudcheflabs.dataroaster.operators.trino;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoClusterStatus;
import com.cloudcheflabs.dataroaster.operators.trino.util.YamlUtils;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.StatusBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@EnableKubernetesMockClient(crud = true)
public class TrinoClusterCRTestRunner {
    KubernetesMockServer server;
    KubernetesClient client;
    private CustomResourceDefinition customResourceDefinition;

    private GenericKubernetesResource customResource;
    private MixedOperation<TrinoCluster, KubernetesResourceList<TrinoCluster>, Resource<TrinoCluster>> trinoClusterClient;

    private TrinoCluster trinoCluster;
    private static final String crdPath = "target/../../chart/templates/trino-clusters.yaml";
    private static final String crPath = "/cr/trino-cluster-etl.yaml";

    @BeforeEach
    public void setup() throws Exception {
        // register custom kind.
        KubernetesDeserializer.registerCustomKind("trino-operator.cloudchef-labs.com/v1alpha1", "TrinoCluster", TrinoCluster.class);

        // load custom resource definition for trino cluster.
        InputStream is = FileUtils.readFile(crdPath);
        customResourceDefinition = client.apiextensions().v1().customResourceDefinitions()
                .load(is).get();

        // create custom resource definition.
        client.apiextensions().v1().customResourceDefinitions().create(customResourceDefinition);

        // load custom resource for a trino cluster.
        InputStream isForCR = FileUtils.readFileFromClasspath(crPath);
        customResource = client.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(customResourceDefinition))
                .load(isForCR).get();

        // create client for trino cluster.
        trinoClusterClient = client.resources(TrinoCluster.class);

        // create object from custom resource.
        trinoCluster = trinoClusterClient.load(FileUtils.readFileFromClasspath(crPath)).get();
    }

    @Test
    public void loadCR() throws Exception {
        assertThat(customResource)
                .isNotNull()
                .hasFieldOrPropertyWithValue("metadata.name", "trino-cluster-etl");
    }

    @Test
    public void request() throws Exception {
        // When
        trinoClusterClient.inNamespace("trino-operator").list();

        // Then
        assertThat(server.getLastRequest())
                .hasFieldOrPropertyWithValue("path", "/apis/trino-operator.cloudchef-labs.com/v1alpha1/namespaces/trino-operator/trinoclusters");
    }

    @Test
    public void create() throws Exception {

        server.expect().post().withPath("/apis/trino-operator.cloudchef-labs.com/v1alpha1/namespaces/trino")
                .andReturn(HttpURLConnection.HTTP_INTERNAL_ERROR, new StatusBuilder().build()).once();
        server.expect().post().withPath("/apis/trino-operator.cloudchef-labs.com/v1alpha1/namespaces/trino")
                .andReturn(HttpURLConnection.HTTP_CREATED, customResource).once();
        server.expect().post().withPath("/apis/trino-operator.cloudchef-labs.com/v1alpha1/namespaces/trino")
                .andReturn(HttpURLConnection.HTTP_CONFLICT, customResource).once();
        server.expect().put().withPath("/apis/trino-operator.cloudchef-labs.com/v1alpha1/namespaces/trino-operator")
                .andReturn(HttpURLConnection.HTTP_OK, customResource).once();
        server.expect().get().withPath("/apis/trino-operator.cloudchef-labs.com/v1alpha1/namespaces/trino-operator")
                .andReturn(HttpURLConnection.HTTP_OK, customResource).once();

        GenericKubernetesResource resource = client.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(customResourceDefinition)).inNamespace("trino-operator")
                .createOrReplace(customResource);
        assertEquals("trino-cluster-etl", resource.getMetadata().getName());

        resource = client.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(customResourceDefinition)).inNamespace("trino-operator")
                .createOrReplace(customResource);
        assertEquals("trino-cluster-etl", resource.getMetadata().getName());
    }

    @Test
    public void get() throws Exception {
        server.expect().get().withPath("/apis/trino-operator.cloudchef-labs.com/v1alpha1/namespaces/trino-operator/trinoclusters/trino-cluster-etl")
                .andReturn(HttpURLConnection.HTTP_OK, customResource).once();

        GenericKubernetesResource customResource = client.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(customResourceDefinition))
                .inNamespace("trino-operator").withName("trino-cluster-etl").get();
        assertNotNull(customResource);
        assertEquals("trino-cluster-etl", customResource.getMetadata().getName());
    }

    @Test
    public void delete() throws Exception {
        // Given
        server.expect().delete().withPath("/apis/trino-operator.cloudchef-labs.com/v1alpha1/namespaces/trino-operator/trinoclusters/trino-cluster-etl").andReturn(
                        HttpURLConnection.HTTP_OK,
                        customResource)
                .once();

        // When
        boolean result = client.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(customResourceDefinition)).inNamespace("trino-operator")
                .withName("trino-cluster-etl").delete();

        // Then
        assertTrue(result);


        server.expect().delete().withPath("/apis/trino-operator.cloudchef-labs.com/v1alpha1/namespaces/trino-operator/trinoclusters/trino-cluster-etl")
                .andReturn(HttpURLConnection.HTTP_NOT_FOUND, customResource).once();

        // When
        boolean deleted = client.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(customResourceDefinition)).inNamespace("trino-operator")
                .withName("trino-cluster-etl").delete();

        // Then
        Assert.assertTrue(!deleted);
    }

    @Test
    public void createCustomResourceWithObject() throws Exception {
        // create custom resource of trino cluster.
        trinoClusterClient.inNamespace("trino-operator").create(trinoCluster);

        KubernetesResourceList<TrinoCluster> trinoClusterList = trinoClusterClient.inNamespace("trino-operator").list();
        Assert.assertNotNull(trinoClusterList);
        Assert.assertEquals(1, trinoClusterList.getItems().size());
    }


    @Test
    public void list() throws Exception {
        // create custom resource of trino cluster.
        trinoClusterClient.inNamespace("trino-operator").create(trinoCluster);

        KubernetesResourceList<TrinoCluster> trinoClusterList = trinoClusterClient.inNamespace("trino-operator").list();
        Assert.assertNotNull(trinoClusterList);
        Assert.assertEquals(1, trinoClusterList.getItems().size());

        TrinoCluster trinoCluster = trinoClusterList.getItems().get(0);
        trinoCluster.getMetadata().setName("trino-for-etl");
        trinoCluster.getSpec().setNamespace("ns-trino-for-etl");

        // create another custom resource.
        trinoClusterClient.inNamespace("trino-operator").create(trinoCluster);

        trinoClusterList = trinoClusterClient.inNamespace("trino-operator").list();
        Assert.assertNotNull(trinoClusterList);
        Assert.assertEquals(2, trinoClusterList.getItems().size());

        trinoClusterList.getItems().forEach(t -> {
            String name = t.getMetadata().getName();
            String ns = t.getSpec().getNamespace();
            if(name.equals("trino-for-etl")) {
                Assert.assertEquals("ns-trino-for-etl", ns);
            } else if(name.equals("trino-cluster-etl")) {
                Assert.assertEquals("trino-cluster-etl", ns);
            }
        });
    }

    @Test
    public void statusSubresourceHandling() throws Exception {
        TrinoClusterStatus trinoClusterStatus = new TrinoClusterStatus();
        trinoClusterStatus.setReplicas(1);
        trinoCluster.setStatus(trinoClusterStatus);

        // set status.
        TrinoCluster retTrinoCluster = trinoClusterClient.inNamespace("trino-operator").create(trinoCluster);
        System.out.printf("trino cluster after setting status: \n%s", YamlUtils.objectToYaml(retTrinoCluster));
        Assert.assertNotNull(retTrinoCluster.getStatus());

        String originalUid = retTrinoCluster.getMetadata().getUid();

        Map<String, String> labels = new HashMap<>();
        labels.put("app", "trino-cluster");

        trinoCluster.getMetadata().setLabels(labels);
        trinoCluster.getMetadata().setResourceVersion("1");

        // add labels and set resource version.
        retTrinoCluster = trinoClusterClient.inNamespace("trino-operator").replace(trinoCluster);
        System.out.printf("trino cluster after adding labels: \n%s", YamlUtils.objectToYaml(retTrinoCluster));
        Assert.assertNotNull(retTrinoCluster.getMetadata().getLabels());

        // set status to null.
        trinoCluster.setStatus(null);
        retTrinoCluster = trinoClusterClient.inNamespace("trino-operator").replace(trinoCluster);
        System.out.printf("trino cluster after setting null status: \n%s", YamlUtils.objectToYaml(retTrinoCluster));
        Assert.assertNull(retTrinoCluster.getStatus());

        // add label to the existing labels.
        labels.put("another", "value");
        retTrinoCluster = trinoClusterClient.inNamespace("trino-operator").withName("trino-cluster-etl").patch(trinoCluster);
        assertEquals(new HashSet<String>(Arrays.asList("app", "another")), retTrinoCluster.getMetadata().getLabels().keySet());

        assertEquals(originalUid, retTrinoCluster.getMetadata().getUid());

        trinoClusterStatus = new TrinoClusterStatus();
        trinoClusterStatus.setReplicas(2);
        trinoCluster.setStatus(trinoClusterStatus);
        retTrinoCluster = trinoClusterClient.inNamespace("trino-operator").withName("trino-cluster-etl").patch(trinoCluster);
        assertNotNull(retTrinoCluster.getStatus());
        Assert.assertEquals(2, retTrinoCluster.getStatus().getReplicas());
        System.out.printf("trino cluster after setting new status: \n%s", YamlUtils.objectToYaml(retTrinoCluster));
    }
}
