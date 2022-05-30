package com.cloudcheflabs.dataroaster.operators.spark.domain;

import com.cloudcheflabs.dataroaster.operators.spark.crd.PodTemplate;

import java.util.HashMap;
import java.util.Map;

public class Pod {
    private String apiVersion = "v1";
    private String kind = "Pod";
    private Metadata metadata = new Metadata();
    private PodTemplate spec;

    public PodTemplate getSpec() {
        return spec;
    }

    public void setSpec(PodTemplate spec) {
        this.spec = spec;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public static class Metadata {
        private String name = "spark-application-pod-template";
        private Map<String, String> labels = new HashMap<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, String> getLabels() {
            return labels;
        }

        public void setLabels(Map<String, String> labels) {
            this.labels = labels;
        }

        public Metadata() {
            labels.put("spark-pod-template-used", "true");
        }
    }

}
