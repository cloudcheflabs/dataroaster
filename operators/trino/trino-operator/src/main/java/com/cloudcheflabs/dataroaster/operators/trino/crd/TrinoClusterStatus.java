package com.cloudcheflabs.dataroaster.operators.trino.crd;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Map;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class TrinoClusterStatus {
    private int replicas;
    private Map<String, Object> additionalProperties;

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }
}
