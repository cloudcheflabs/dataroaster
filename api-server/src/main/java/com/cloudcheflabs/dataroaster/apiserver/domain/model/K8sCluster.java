package com.cloudcheflabs.dataroaster.apiserver.domain.model;

import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "k8s_cluster")
public class K8sCluster implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "cluster_name")
    private String clusterName;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "k8sCluster", fetch = FetchType.EAGER)
    private Set<K8sNamespace> k8sNamespaceSet = Sets.newHashSet();


    @OneToMany(mappedBy = "k8sCluster", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<K8sKubeconfig> k8sKubeconfigSet = Sets.newHashSet();

    public Set<K8sKubeconfig> getK8sKubeconfigSet() {
        return k8sKubeconfigSet;
    }

    public void setK8sKubeconfigSet(Set<K8sKubeconfig> k8sKubeconfigSet) {
        this.k8sKubeconfigSet = k8sKubeconfigSet;
    }

    public Set<K8sNamespace> getK8sNamespaceSet() {
        return k8sNamespaceSet;
    }

    public void setK8sNamespaceSet(Set<K8sNamespace> k8sNamespaceSet) {
        this.k8sNamespaceSet = k8sNamespaceSet;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
