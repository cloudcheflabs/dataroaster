package com.cloudcheflabs.dataroaster.trino.controller.api.service;

import com.cloudcheflabs.dataroaster.trino.controller.domain.CustomResource;

public interface K8sResourceService {
    void createCustomResource(CustomResource customResource);

    void deleteCustomResource(String name, String namespace, String kind);

    void updateCustomResource(CustomResource customResource);
}
