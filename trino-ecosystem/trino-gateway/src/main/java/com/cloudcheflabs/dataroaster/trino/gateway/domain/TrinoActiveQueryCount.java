package com.cloudcheflabs.dataroaster.trino.gateway.domain;

import java.io.Serializable;

public class TrinoActiveQueryCount implements Serializable {

    private String clusterName;
    private String coordinatorAddress;
    private int count;

    public TrinoActiveQueryCount() {}

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getCoordinatorAddress() {
        return coordinatorAddress;
    }

    public void setCoordinatorAddress(String coordinatorAddress) {
        this.coordinatorAddress = coordinatorAddress;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
