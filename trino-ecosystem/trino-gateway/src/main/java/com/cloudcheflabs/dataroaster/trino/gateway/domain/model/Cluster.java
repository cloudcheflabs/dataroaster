package com.cloudcheflabs.dataroaster.trino.gateway.domain.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cluster")
public class Cluster implements Serializable {

    @Id
    @Column(name = "cluster_name")
    private String clusterName;

    @Column(name = "cluster_type")
    private String clusterType;

    @Column(name = "url")
    private String url;

    @Column(name = "activated")
    private boolean activated;

    @ManyToOne
    @JoinColumn(name ="group_name")
    private ClusterGroup clusterGroup;


    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public ClusterGroup getClusterGroup() {
        return clusterGroup;
    }

    public void setClusterGroup(ClusterGroup clusterGroup) {
        this.clusterGroup = clusterGroup;
    }
}
