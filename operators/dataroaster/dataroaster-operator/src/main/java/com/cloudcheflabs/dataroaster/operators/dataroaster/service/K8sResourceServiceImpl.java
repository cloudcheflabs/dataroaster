package com.cloudcheflabs.dataroaster.operators.dataroaster.service;

import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.CustomResourceDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.K8sResourceDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.K8sResourceService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class K8sResourceServiceImpl implements K8sResourceService {

    @Autowired
    @Qualifier("kubernetesK8sResourceDao")
    private K8sResourceDao k8sResourceDao;

    @Autowired
    @Qualifier("hibernateCustomResourceDao")
    private CustomResourceDao customResourceDao;

    @Override
    public void createCustomResource(CustomResource customResource) {
        k8sResourceDao.createCustomResource(customResource);
        customResourceDao.create(customResource);
    }

    @Override
    public void deleteCustomResource(String name, String namespace) {

    }

    @Override
    public void updateCustomResource(CustomResource customResource) {

    }
}
