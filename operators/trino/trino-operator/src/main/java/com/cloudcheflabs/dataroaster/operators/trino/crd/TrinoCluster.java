package com.cloudcheflabs.dataroaster.operators.trino.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version(TrinoCluster.VERSION)
@Group(TrinoCluster.GROUP)
public class TrinoCluster extends CustomResource<TrinoClusterSpec, TrinoClusterStatus> implements Namespaced {

    public static final String GROUP = "trino-operator.cloudchef-labs.com";
    public static final String VERSION = "v1alpha1";



}
