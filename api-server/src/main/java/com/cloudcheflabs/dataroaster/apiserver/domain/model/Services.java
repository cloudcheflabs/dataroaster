package com.cloudcheflabs.dataroaster.apiserver.domain.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "services")
public class Services implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @ManyToOne
    @JoinColumn(name ="service_def_id")
    private ServiceDef serviceDef;

    @ManyToOne
    @JoinColumn(name ="namespace_id")
    private K8sNamespace k8sNamespace;

    @ManyToOne
    @JoinColumn(name ="project_id")
    private Project project;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ServiceDef getServiceDef() {
        return serviceDef;
    }

    public void setServiceDef(ServiceDef serviceDef) {
        this.serviceDef = serviceDef;
    }

    public K8sNamespace getK8sNamespace() {
        return k8sNamespace;
    }

    public void setK8sNamespace(K8sNamespace k8sNamespace) {
        this.k8sNamespace = k8sNamespace;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
