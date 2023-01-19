package com.cloudcheflabs.dataroaster.operators.trino.api.dao;


import com.cloudcheflabs.dataroaster.operators.trino.domain.CustomResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;

import java.util.List;

public interface K8sResourceDao {

    void createCustomResource(CustomResource customResource);

    void deleteCustomResource(String name, String namespace, String kind);

    void updateCustomResource(CustomResource customResource);

    void updateCustomResource(GenericKubernetesResource genericKubernetesResource);

    List<GenericKubernetesResource> listCustomResources(String namespace, String kind);
}
