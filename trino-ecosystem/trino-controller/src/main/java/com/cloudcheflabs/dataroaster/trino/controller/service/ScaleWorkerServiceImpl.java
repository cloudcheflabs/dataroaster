package com.cloudcheflabs.dataroaster.trino.controller.service;

import com.cloudcheflabs.dataroaster.trino.controller.api.dao.ScaleWorkerDao;
import com.cloudcheflabs.dataroaster.trino.controller.api.service.ScaleWorkerService;
import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ScaleWorkerServiceImpl implements ScaleWorkerService {

    @Autowired
    @Qualifier("restScaleWorkerDao")
    private ScaleWorkerDao scaleWorkerDao;

    @Override
    public RestResponse listWorkerCount(String restUri, String namespace) {
        return scaleWorkerDao.listWorkerCount(restUri, namespace);
    }

    @Override
    public RestResponse scaleOutWorkers(String restUri, String namespace, String name, int replicas) {
        return scaleWorkerDao.scaleOutWorkers(restUri, namespace, name, replicas);
    }

    @Override
    public RestResponse listHpa(String restUri, String namespace) {
        return scaleWorkerDao.listHpa(restUri, namespace);
    }

    @Override
    public RestResponse updateHpa(String restUri, String namespace, String name, int minReplicas, int maxReplicas) {
        return scaleWorkerDao.updateHpa(restUri, namespace, name, minReplicas, maxReplicas);
    }
}
