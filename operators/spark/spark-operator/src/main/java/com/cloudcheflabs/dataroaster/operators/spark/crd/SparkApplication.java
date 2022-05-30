package com.cloudcheflabs.dataroaster.operators.spark.crd;


import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version(SparkApplication.VERSION)
@Group(SparkApplication.GROUP)
public class SparkApplication extends CustomResource<SparkApplicationSpec, Void> implements Namespaced {
    public static final String GROUP = "spark-operator.cloudchef-labs.com";
    public static final String VERSION = "v1alpha1";
}
