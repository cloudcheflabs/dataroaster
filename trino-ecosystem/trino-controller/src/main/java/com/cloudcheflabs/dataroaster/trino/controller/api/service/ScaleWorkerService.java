package com.cloudcheflabs.dataroaster.trino.controller.api.service;

import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;

public interface ScaleWorkerService {
    RestResponse listWorkerCount(String restUri, String namespace);
    RestResponse scaleOutWorkers(String restUri, String namespace, String name, int replicas);
    RestResponse listHpa(String restUri, String namespace);
    RestResponse updateHpa(String restUri, String namespace, String name, int minReplicas, int maxReplicas);
}
