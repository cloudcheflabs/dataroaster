package com.cloudcheflabs.dataroaster.operators.trino.handler;

import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrinoClusterActionHandler implements ActionHandler<TrinoCluster> {

    private static Logger LOG = LoggerFactory.getLogger(TrinoClusterActionHandler.class);


    @Autowired
    private TrinoClusterClient trinoClusterClient;

    public TrinoClusterActionHandler() {
    }


    @Override
    public void create(TrinoCluster trinoCluster) {
        // create coordinator.
        CoordinatorHandler coordinatorHandler = new CoordinatorHandler(trinoClusterClient.getClient());
        coordinatorHandler.create(trinoCluster);
        LOG.info("coordinator created...");

        // create workers.
        WorkerHandler workerHandler = new WorkerHandler(trinoClusterClient.getClient());
        workerHandler.create(trinoCluster);
        LOG.info("worker created...");

    }

    @Override
    public void destroy(TrinoCluster trinoCluster) {
        // delete coordinator.
        CoordinatorHandler coordinatorHandler = new CoordinatorHandler(trinoClusterClient.getClient());
        coordinatorHandler.delete(trinoCluster);
        LOG.info("coordinator deleted...");

        // delete workers.
        WorkerHandler workerHandler = new WorkerHandler(trinoClusterClient.getClient());
        workerHandler.delete(trinoCluster);
        LOG.info("worker deleted...");
    }
}
