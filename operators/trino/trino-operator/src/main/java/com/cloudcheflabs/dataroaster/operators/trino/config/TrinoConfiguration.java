package com.cloudcheflabs.dataroaster.operators.trino.config;

public class TrinoConfiguration {

    public static final String DEFAULT_MASTER = System.getProperty("masterUrlForLocalTest", "k8s://https://kubernetes.default.svc");
    public static final String DEFAULT_TRINO_OPERATOR_NAMESPACE = "trino-operator";

    /**
     * coordinator
     */
    public static final String DEFAULT_COORDINATOR_CONFIGMAP = "trino-coordinator";
    public static final String DEFAULT_COORDINATOR_DEPLOYMENT = DEFAULT_COORDINATOR_CONFIGMAP;
    public static final String DEFAULT_COORDINATOR_SERVICE = "trino-coordinator-service";

    public static final String COORDINATOR_RMI_REGISTRY_SERVICE = "trino-coordinator-rmiregistry-service";

    public static final String COORDINATOR_JMX_EXPORTER_SERVICE = "trino-coordinator-jmxexporter-service";

    /**
     * worker
     */
    public static final String DEFAULT_WORKER_CONFIGMAP = "trino-worker";
    public static final String DEFAULT_WORKER_DEPLOYMENT = DEFAULT_WORKER_CONFIGMAP;
    public static final String DEFAULT_WORKER_HPA = DEFAULT_WORKER_CONFIGMAP;

    public static final String WORKER_RMI_REGISTRY_SERVICE = "trino-worker-rmiregistry-service";

    public static final String WORKER_JMX_EXPORTER_SERVICE = "trino-worker-jmxexporter-service";
}
