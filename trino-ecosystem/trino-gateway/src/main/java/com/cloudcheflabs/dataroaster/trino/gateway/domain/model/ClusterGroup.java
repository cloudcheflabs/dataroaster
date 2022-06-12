package com.cloudcheflabs.dataroaster.trino.gateway.domain.model;

import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "cluster_group")
public class ClusterGroup implements Serializable {
    @Id
    @Column(name = "group_name")
    private String groupName;

    @OneToMany(mappedBy = "clusterGroup", fetch = FetchType.EAGER)
    private Set<Cluster> clusterSet = Sets.newHashSet();

    @OneToMany(mappedBy = "clusterGroup", fetch = FetchType.EAGER)
    private Set<Users> usersSet = Sets.newHashSet();

    public Set<Users> getUsersSet() {
        return usersSet;
    }

    public void setUsersSet(Set<Users> usersSet) {
        this.usersSet = usersSet;
    }

    public Set<Cluster> getClusterSet() {
        return clusterSet;
    }

    public void setClusterSet(Set<Cluster> clusterSet) {
        this.clusterSet = clusterSet;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
