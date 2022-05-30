package com.cloudcheflabs.dataroaster.apiserver.domain;

public class Kubeconfig {
    private String masterUrl;
    private String clusterName;
    private String clusterCertData;
    private String namespace;
    private String user;
    private String clientCertData;
    private String clientKeyData;
    private String rawKubeconfig;

    public Kubeconfig() {}

    public Kubeconfig(String masterUrl,
                      String clusterName,
                      String clusterCertData,
                      String namespace,
                      String user,
                      String clientCertData,
                      String clientKeyData,
                      String rawKubeconfig) {
        this.masterUrl = masterUrl;
        this.clusterName = clusterName;
        this.clusterCertData = clusterCertData;
        this.namespace = namespace;
        this.user = user;
        this.clientCertData = clientCertData;
        this.clientKeyData = clientKeyData;
        this.rawKubeconfig = rawKubeconfig;
    }

    public String getRawKubeconfig() {
        return rawKubeconfig;
    }

    public String getMasterUrl() {
        return masterUrl;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getClusterCertData() {
        return clusterCertData;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getUser() {
        return user;
    }

    public String getClientCertData() {
        return clientCertData;
    }

    public String getClientKeyData() {
        return clientKeyData;
    }
}
