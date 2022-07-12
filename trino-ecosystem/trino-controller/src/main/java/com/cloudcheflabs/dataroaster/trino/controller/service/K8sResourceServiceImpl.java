package com.cloudcheflabs.dataroaster.trino.controller.service;

import com.cloudcheflabs.dataroaster.trino.controller.api.dao.K8sResourceDao;
import com.cloudcheflabs.dataroaster.trino.controller.api.service.K8sResourceService;
import com.cloudcheflabs.dataroaster.trino.controller.domain.CustomResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class K8sResourceServiceImpl implements K8sResourceService {

    @Autowired
    @Qualifier("kubernetesK8sResourceDao")
    private K8sResourceDao k8sResourceDao;

    @Override
    public void createCustomResource(CustomResource customResource) {
        k8sResourceDao.createCustomResource(customResource);
    }

    @Override
    public void deleteCustomResource(String name, String namespace, String kind) {
        k8sResourceDao.deleteCustomResource(name, namespace, kind);
    }

    @Override
    public void updateCustomResource(CustomResource customResource) {
        k8sResourceDao.updateCustomResource(customResource);
    }

    @Override
    public List<GenericKubernetesResource> listCustomResources(String namespace, String kind) {
        return k8sResourceDao.listCustomResources(namespace, kind);
    }
}
