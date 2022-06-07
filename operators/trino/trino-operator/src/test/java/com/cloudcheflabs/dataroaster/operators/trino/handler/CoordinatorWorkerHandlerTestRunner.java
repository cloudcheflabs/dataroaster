package com.cloudcheflabs.dataroaster.operators.trino.handler;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

@EnableKubernetesMockClient(crud = true)
public class CoordinatorWorkerHandlerTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(CoordinatorWorkerHandlerTestRunner.class);

    KubernetesMockServer server;
    KubernetesClient client;
    private TrinoCluster trinoCluster;

    private MixedOperation<TrinoCluster, KubernetesResourceList<TrinoCluster>, Resource<TrinoCluster>> trinoClusterClient;

    private static final String crdPath = "target/../../chart/templates/trino-clusters.yaml";
    private static final String crPath = "/cr/trino-cluster-etl.yaml";

    @BeforeEach
    public void setup() throws Exception {

        // load custom resource definition for trino cluster.
        InputStream is = FileUtils.readFile(crdPath);
        CustomResourceDefinition customResourceDefinition = client.apiextensions().v1().customResourceDefinitions()
                .load(is).get();

        // create custom resource definition.
        client.apiextensions().v1().customResourceDefinitions().create(customResourceDefinition);

        // load custom resource for a trino cluster.
        InputStream isForCR = FileUtils.readFileFromClasspath(crPath);
        GenericKubernetesResource customResource = client.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(customResourceDefinition))
                .load(isForCR).get();

        // create client for trino cluster.
        trinoClusterClient = client.resources(TrinoCluster.class);

        // create object from custom resource.
        trinoCluster = trinoClusterClient.load(FileUtils.readFileFromClasspath(crPath)).get();
    }

    @Test
    public void createCoordinator() throws Exception {
        CoordinatorHandler coordinatorHandler = new CoordinatorHandler(client);
        coordinatorHandler.create(trinoCluster);
    }

    @Test
    public void deleteCoordinator() throws Exception {
        CoordinatorHandler coordinatorHandler = new CoordinatorHandler(client);
        coordinatorHandler.create(trinoCluster);
        coordinatorHandler.delete(trinoCluster);
    }

    @Test
    public void createWorker() throws Exception {
        WorkerHandler workerHandler = new WorkerHandler(client);
        workerHandler.create(trinoCluster);
    }

    @Test
    public void deleteWorker() throws Exception {
        WorkerHandler workerHandler = new WorkerHandler(client);
        workerHandler.create(trinoCluster);
        workerHandler.delete(trinoCluster);
    }
}
