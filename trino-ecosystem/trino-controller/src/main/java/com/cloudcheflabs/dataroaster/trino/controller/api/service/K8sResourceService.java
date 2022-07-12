package com.cloudcheflabs.dataroaster.trino.controller.api.service;

import com.cloudcheflabs.dataroaster.trino.controller.domain.CustomResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;

import java.util.List;

public interface K8sResourceService {
    void createCustomResource(CustomResource customResource);

    void deleteCustomResource(String name, String namespace, String kind);

    void updateCustomResource(CustomResource customResource);

    List<GenericKubernetesResource> listCustomResources(String namespace, String kind);
}
