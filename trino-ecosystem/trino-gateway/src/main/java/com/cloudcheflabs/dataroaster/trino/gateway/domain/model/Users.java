package com.cloudcheflabs.dataroaster.trino.gateway.domain.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "users")
public class Users implements Serializable {

    @Id
    @Column(name = "user")
    private String user;

    @Column(name = "password")
    private String password;

    @ManyToOne
    @JoinColumn(name ="group_name")
    private ClusterGroup clusterGroup;



    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ClusterGroup getClusterGroup() {
        return clusterGroup;
    }

    public void setClusterGroup(ClusterGroup clusterGroup) {
        this.clusterGroup = clusterGroup;
    }
}
