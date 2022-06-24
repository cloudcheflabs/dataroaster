package com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "custom_resource")
public class CustomResource implements Serializable {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "kind")
    private String kind;

    @Column(name = "name")
    private String name;

    @Column(name = "namespace")
    private String namespace;

    @Column(name = "yaml")
    private String yaml;

    @ManyToOne
    @JoinColumn(name ="comp_name")
    private Components components;

    public Components getComponents() {
        return components;
    }

    public void setComponents(Components components) {
        this.components = components;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getYaml() {
        return yaml;
    }

    public void setYaml(String yaml) {
        this.yaml = yaml;
    }
}
