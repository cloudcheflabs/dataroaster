package com.cloudcheflabs.dataroaster.apiserver.domain.model;

import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "k8s_namespace")
public class K8sNamespace implements Serializable {

    // ingress controller.
    public static final String DEFAULT_NAMESPACE_INGRESS_CONTROLLER_NGINX = "ingress-nginx";
    public static final String DEFAULT_NAMESPACE_CERT_MANAGER = "cert-manager";

    // pod log monitoring.
    public static final String DEFAULT_NAMESPACE_FILEBEAT = "dataroaster-filebeat";
    public static final String DEFAULT_NAMESPACE_LOGSTASH = "dataroaster-logstash";

    // metrics monitoring.
    public static final String DEFAULT_NAMESPACE_PROM_STACK = "dataroaster-prom-stack";

    // distributed tracing.
    public static final String DEFAULT_NAMESPACE_JAEGER = "dataroaster-jaeger";

    // private registry.
    public static final String DEFAULT_NAMESPACE_HARBOR = "dataroaster-harbor";

    // ci / cd.
    public static final String DEFAULT_NAMESPACE_ARGOCD = "dataroaster-argocd";
    public static final String DEFAULT_NAMESPACE_JENKINS = "dataroaster-jenkins";

    // backup.
    public static final String DEFAULT_NAMESPACE_VELERO = "dataroaster-velero";

    // data catalog.
    public static final String DEFAULT_NAMESPACE_HIVE_METASTORE = "dataroaster-hivemetastore";

    // query engine.
    public static final String DEFAULT_NAMESPACE_SPARK_THRIFT_SERVER = "dataroaster-spark-thrift-server";
    public static final String DEFAULT_NAMESPACE_TRINO = "dataroaster-trino";

    // streaming.
    public static final String DEFAULT_NAMESPACE_KAFKA = "dataroaster-kafka";

    // analytics.
    public static final String DEFAULT_NAMESPACE_JUPYTERHUB = "dataroaster-jupyterhub";
    public static final String DEFAULT_NAMESPACE_REDASH = "dataroaster-redash";

    // workflow.
    public static final String DEFAULT_NAMESPACE_ARGO_WORKFLOW = "dataroaster-argo-workflow";



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "namespace_name")
    private String namespaceName;

    @ManyToOne
    @JoinColumn(name ="cluster_id")
    private K8sCluster k8sCluster;


    @OneToMany(mappedBy = "k8sNamespace", fetch = FetchType.EAGER)
    private Set<Services> servicesSet = Sets.newHashSet();


    public Set<Services> getServicesSet() {
        return servicesSet;
    }

    public void setServicesSet(Set<Services> servicesSet) {
        this.servicesSet = servicesSet;
    }

    public K8sCluster getK8sCluster() {
        return k8sCluster;
    }

    public void setK8sCluster(K8sCluster k8sCluster) {
        this.k8sCluster = k8sCluster;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }
}
