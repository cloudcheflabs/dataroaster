package com.cloudcheflabs.dataroaster.operators.trino;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.operators.trino.util.YamlUtils;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@EnableKubernetesMockClient
public class TrinoClusterCRDTestRunner {

    KubernetesMockServer server;
    KubernetesClient client;

    private CustomResourceDefinition customResourceDefinition;

    private static final String crdPath = "target/../../chart/templates/trino-clusters.yaml";

    @BeforeEach
    public void setupCRD() throws Exception {
        InputStream is = FileUtils.readFile(crdPath);

        customResourceDefinition = client.apiextensions().v1().customResourceDefinitions()
                .load(is).get();
    }

    @Test
    public void loadCRD() throws Exception {
        InputStream is = FileUtils.readFile(crdPath);

        CustomResourceDefinition customResourceDefinition = client.apiextensions().v1().customResourceDefinitions()
                .load(is).get();

        String yaml = YamlUtils.objectToYaml(customResourceDefinition);
        System.out.println(yaml);

        assertNotNull(customResourceDefinition);
        assertEquals("trinoclusters.trino-operator.cloudchef-labs.com", customResourceDefinition.getMetadata().getName());
    }

    @Test
    public void get() throws Exception {
        server.expect().get().withPath("/apis/apiextensions.k8s.io/v1/customresourcedefinitions/trinoclusters.trino-operator.cloudchef-labs.com")
                .andReturn(200, customResourceDefinition).once();

        CustomResourceDefinition crd = client.apiextensions().v1().customResourceDefinitions()
                .withName("trinoclusters.trino-operator.cloudchef-labs.com").get();
        assertNotNull(crd);
        assertEquals("trinoclusters.trino-operator.cloudchef-labs.com", crd.getMetadata().getName());
    }

    @Test
    public void create() throws Exception {
        server.expect().post().withPath("/apis/apiextensions.k8s.io/v1/customresourcedefinitions")
                .andReturn(200, customResourceDefinition).once();

        CustomResourceDefinition crd = client.apiextensions().v1().customResourceDefinitions()
                .createOrReplace(customResourceDefinition);
        assertNotNull(crd);
        assertEquals("trinoclusters.trino-operator.cloudchef-labs.com", crd.getMetadata().getName());
    }

    @Test
    public void list() throws Exception {
        server.expect().get().withPath("/apis/apiextensions.k8s.io/v1/customresourcedefinitions")
                .andReturn(200, new KubernetesListBuilder().withItems(customResourceDefinition).build()).once();

        CustomResourceDefinitionList crdList = client.apiextensions().v1().customResourceDefinitions().list();
        assertNotNull(crdList);
        assertEquals(1, crdList.getItems().size());
        assertEquals("trinoclusters.trino-operator.cloudchef-labs.com", crdList.getItems().get(0).getMetadata().getName());
    }

    @Test
    public void delete() throws Exception {
        server.expect().delete()
                .withPath("/apis/apiextensions.k8s.io/v1/customresourcedefinitions/trinoclusters.trino-operator.cloudchef-labs.com")
                .andReturn(200, customResourceDefinition).once();

        boolean deleted = client.apiextensions().v1().customResourceDefinitions().withName("trinoclusters.trino-operator.cloudchef-labs.com")
                .delete();
        assertTrue(deleted);
    }

}
