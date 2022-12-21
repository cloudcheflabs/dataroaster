package com.cloudcheflabs.dataroaster.trino.gateway.domain;

import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Cluster;

import java.io.Serializable;

public class ClusterWithActiveQueryCount implements Serializable {

    private Cluster cluster;
    private TrinoActiveQueryCount trinoActiveQueryCount;


    public ClusterWithActiveQueryCount(Cluster cluster, TrinoActiveQueryCount trinoActiveQueryCount) {
        this.cluster = cluster;
        this.trinoActiveQueryCount = trinoActiveQueryCount;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public TrinoActiveQueryCount getTrinoActiveQueryCount() {
        return trinoActiveQueryCount;
    }
}
