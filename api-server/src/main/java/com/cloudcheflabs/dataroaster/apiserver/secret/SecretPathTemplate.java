package com.cloudcheflabs.dataroaster.apiserver.secret;

public class SecretPathTemplate {

    public static final String SECRET_KUBECONFIG = "secret/kubeconfig/{{ clusterId }}/{{ user }}";

}
