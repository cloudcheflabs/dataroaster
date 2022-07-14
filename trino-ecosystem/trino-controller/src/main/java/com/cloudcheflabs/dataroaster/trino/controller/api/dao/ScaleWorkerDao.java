package com.cloudcheflabs.dataroaster.trino.controller.api.dao;

import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;

public interface ScaleWorkerDao {

    RestResponse listWorkerCount(String restUri, String namespace);
    RestResponse scaleOutWorkers(String restUri, String namespace, String name, int replicas);
    RestResponse listHpa(String restUri, String namespace);
    RestResponse updateHpa(String restUri, String namespace, String name, int minReplicas, int maxReplicas);
}
