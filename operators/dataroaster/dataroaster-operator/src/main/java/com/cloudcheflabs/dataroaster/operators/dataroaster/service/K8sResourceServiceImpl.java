package com.cloudcheflabs.dataroaster.operators.dataroaster.service;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.CustomResourceDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.K8sResourceDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.K8sResourceService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class K8sResourceServiceImpl implements K8sResourceService {

    private static Logger LOG = LoggerFactory.getLogger(K8sResourceServiceImpl.class);

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
    public void deleteCustomResource(String name, String namespace, String kind) {
        k8sResourceDao.deleteCustomResource(name, namespace, kind);
        CustomResource retCustomResource = customResourceDao
                .findCustomResource(name, namespace, kind);
        if(retCustomResource != null) {
            customResourceDao.delete(retCustomResource);
        } else {
            LOG.warn("custom resource [{}] not found in db!", name);
        }
    }

    @Override
    public void updateCustomResource(CustomResource customResource) {
        k8sResourceDao.updateCustomResource(customResource);

        CustomResource retCustomResource = customResourceDao
                .findCustomResource(customResource.getName(), customResource.getNamespace(), customResource.getKind());
        if(retCustomResource == null) {
            LOG.warn("customResource [{}] not found in db!", JsonUtils.toJson(customResource));
            customResourceDao.create(customResource);
        } else {
            retCustomResource.setYaml(customResource.getYaml());
            customResourceDao.update(retCustomResource);
        }
    }
}
