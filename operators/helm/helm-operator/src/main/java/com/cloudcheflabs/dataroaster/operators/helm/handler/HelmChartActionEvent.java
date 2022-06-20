package com.cloudcheflabs.dataroaster.operators.helm.handler;

import com.cloudcheflabs.dataroaster.operators.helm.crd.HelmChart;
import io.fabric8.kubernetes.client.Watcher;

public class HelmChartActionEvent {
    private Watcher.Action action;
    private HelmChart helmChart;

    public HelmChartActionEvent(Watcher.Action action, HelmChart helmChart) {
        this.action = action;
        this.helmChart = helmChart;
    }

    public Watcher.Action getAction() {
        return action;
    }

    public HelmChart getHelmChart() {
        return helmChart;
    }
}
