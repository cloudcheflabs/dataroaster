package com.cloudcheflabs.dataroaster.operators.spark.crd;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class Executor {
    private int instances;
    private Map<String, String> label;
    private Map<String, String> annotation;
    private Resources resources;
    private List<VolumeMount> volumeMounts;
    private PodTemplate podTemplate;

    public PodTemplate getPodTemplate() {
        return podTemplate;
    }

    public void setPodTemplate(PodTemplate podTemplate) {
        this.podTemplate = podTemplate;
    }

    public Map<String, String> getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Map<String, String> annotation) {
        this.annotation = annotation;
    }

    public int getInstances() {
        return instances;
    }

    public void setInstances(int instances) {
        this.instances = instances;
    }

    public Map<String, String> getLabel() {
        return label;
    }

    public void setLabel(Map<String, String> label) {
        this.label = label;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public List<VolumeMount> getVolumeMounts() {
        return volumeMounts;
    }

    public void setVolumeMounts(List<VolumeMount> volumeMounts) {
        this.volumeMounts = volumeMounts;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(new ObjectMapper(), this);
    }
}
