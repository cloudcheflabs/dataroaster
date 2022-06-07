package com.cloudcheflabs.dataroaster.operators.trino.crd;

import io.fabric8.kubernetes.api.model.LocalObjectReference;

import java.util.List;

public class Image {
    private String repository;
    private String tag;
    private String imagePullPolicy;
    private List<LocalObjectReference> imagePullSecrets;

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getImagePullPolicy() {
        return imagePullPolicy;
    }

    public void setImagePullPolicy(String imagePullPolicy) {
        this.imagePullPolicy = imagePullPolicy;
    }

    public List<LocalObjectReference> getImagePullSecrets() {
        return imagePullSecrets;
    }

    public void setImagePullSecrets(List<LocalObjectReference> imagePullSecrets) {
        this.imagePullSecrets = imagePullSecrets;
    }
}
