package com.cloudcheflabs.dataroaster.trino.controller.service;

import com.cloudcheflabs.dataroaster.trino.controller.api.dao.K8sResourceDao;
import com.cloudcheflabs.dataroaster.trino.controller.domain.CustomResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class K8sResourceServiceImpl implements K8sResourceDao {

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
}
