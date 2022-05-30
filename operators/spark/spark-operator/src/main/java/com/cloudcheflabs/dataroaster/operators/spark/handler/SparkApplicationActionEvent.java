package com.cloudcheflabs.dataroaster.operators.spark.handler;

import com.cloudcheflabs.dataroaster.operators.spark.crd.SparkApplication;
import io.fabric8.kubernetes.client.Watcher;

public class SparkApplicationActionEvent {
    private Watcher.Action action;
    private SparkApplication sparkApplication;

    public SparkApplicationActionEvent(Watcher.Action action, SparkApplication sparkApplication) {
        this.action = action;
        this.sparkApplication = sparkApplication;
    }

    public Watcher.Action getAction() {
        return action;
    }

    public SparkApplication getSparkApplication() {
        return sparkApplication;
    }
}
