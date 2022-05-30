package com.cloudcheflabs.dataroaster.apiserver.domain.model;

import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "service_def")
public class ServiceDef implements Serializable {

    public static enum ServiceTypeEnum {
        INGRESS_CONTROLLER,
        POD_LOG_MONITORING,
        DISTRIBUTED_TRACING,
        METRICS_MONITORING,
        PRIVATE_REGISTRY,
        CI_CD,
        BACKUP,
        SECRET_MANAGEMENT,
        STORAGE,
        DATA_CATALOG,
        QUERY_ENGINE,
        STREAMING,
        ANALYTICS,
        WORKFLOW
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "type")
    private String type;

    @Column(name = "name")
    private String name;

    @Column(name = "version")
    private String version;

    @Column(name = "external")
    private boolean external;

    @OneToMany(mappedBy = "serviceDef", fetch = FetchType.EAGER)
    private Set<Services> servicesSet = Sets.newHashSet();

    public Set<Services> getServicesSet() {
        return servicesSet;
    }

    public void setServicesSet(Set<Services> servicesSet) {
        this.servicesSet = servicesSet;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
