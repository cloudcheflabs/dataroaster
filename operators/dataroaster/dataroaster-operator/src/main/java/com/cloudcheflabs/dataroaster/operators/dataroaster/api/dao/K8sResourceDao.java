package com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao;

import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;

public interface K8sResourceDao {

    void createCustomResource(CustomResource customResource);

    void deleteCustomResource(String name, String namespace);

    void updateCustomResource(CustomResource customResource);
}
