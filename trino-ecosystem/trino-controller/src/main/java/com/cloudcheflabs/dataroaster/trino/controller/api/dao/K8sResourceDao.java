package com.cloudcheflabs.dataroaster.trino.controller.api.dao;


import com.cloudcheflabs.dataroaster.trino.controller.domain.CustomResource;

public interface K8sResourceDao {

    void createCustomResource(CustomResource customResource);

    void deleteCustomResource(String name, String namespace, String kind);

    void updateCustomResource(CustomResource customResource);
}
