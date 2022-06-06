package com.cloudcheflabs.dataroaster.operators.trino.handler;

import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import io.fabric8.kubernetes.client.Watcher;

public class TrinoClusterActionEvent {
    private Watcher.Action action;
    private TrinoCluster trinoCluster;

    public TrinoClusterActionEvent(Watcher.Action action, TrinoCluster trinoCluster) {
        this.action = action;
        this.trinoCluster = trinoCluster;
    }

    public Watcher.Action getAction() {
        return action;
    }

    public TrinoCluster getTrinoCluster() {
        return trinoCluster;
    }
}
