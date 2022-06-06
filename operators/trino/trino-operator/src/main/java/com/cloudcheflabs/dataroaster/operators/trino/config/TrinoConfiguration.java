package com.cloudcheflabs.dataroaster.operators.trino.config;

public class TrinoConfiguration {

    public static final String DEFAULT_MASTER = System.getProperty("masterUrlForLocalTest", "k8s://https://kubernetes.default.svc");
    public static final String DEFAULT_TRINO_OPERATOR_NAMESPACE = "trino-operator";


}
