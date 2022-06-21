package com.cloudcheflabs.dataroaster.operators.dataroaster.api.service;

import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;

public interface K8sResourceService {

    void createCustomResource(CustomResource customResource);

    void deleteCustomResource(String name, String namespace);

    void updateCustomResource(CustomResource customResource);
}
