package com.cloudcheflabs.dataroaster.operators.helm.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version(HelmChart.VERSION)
@Group(HelmChart.GROUP)
public class HelmChart extends CustomResource<HelmChartSpec, Void> implements Namespaced {

    public static final String GROUP = "helm-operator.cloudchef-labs.com";
    public static final String VERSION = "v1beta1";



}
