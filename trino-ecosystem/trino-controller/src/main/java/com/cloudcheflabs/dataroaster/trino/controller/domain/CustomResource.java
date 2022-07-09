package com.cloudcheflabs.dataroaster.trino.controller.domain;

import java.io.Serializable;

public class CustomResource implements Serializable {

    private String kind;
    private String name;
    private String namespace;
    private String yaml;

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
