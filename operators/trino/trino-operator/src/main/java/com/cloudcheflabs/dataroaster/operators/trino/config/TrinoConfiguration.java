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

    /**
     * worker
     */
    public static final String DEFAULT_WORKER_CONFIGMAP = "trino-worker";
    public static final String DEFAULT_WORKER_DEPLOYMENT = DEFAULT_WORKER_CONFIGMAP;
}
