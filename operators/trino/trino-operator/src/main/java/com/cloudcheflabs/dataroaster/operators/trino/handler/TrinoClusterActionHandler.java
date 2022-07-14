package com.cloudcheflabs.dataroaster.operators.trino.handler;

import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoClusterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cloudcheflabs.dataroaster.operators.trino.config.TrinoConfiguration.DEFAULT_COORDINATOR_DEPLOYMENT;
import static com.cloudcheflabs.dataroaster.operators.trino.config.TrinoConfiguration.DEFAULT_WORKER_DEPLOYMENT;

public class TrinoClusterActionHandler implements ActionHandler<TrinoCluster> {

    private static Logger LOG = LoggerFactory.getLogger(TrinoClusterActionHandler.class);

    private TrinoClusterClient trinoClusterClient;

    public TrinoClusterActionHandler(TrinoClusterClient trinoClusterClient) {
        this.trinoClusterClient = trinoClusterClient;
    }


    @Override
    public void create(TrinoCluster trinoCluster) {
        try {
            // create coordinator.
            CoordinatorHandler coordinatorHandler = new CoordinatorHandler(trinoClusterClient.getClient());
            coordinatorHandler.create(trinoCluster);
            LOG.info("coordinator created...");

            // create workers.
            WorkerHandler workerHandler = new WorkerHandler(trinoClusterClient.getClient());
            workerHandler.create(trinoCluster);
            LOG.info("worker created...");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void update(TrinoCluster trinoCluster) {
        create(trinoCluster);
    }

    @Override
    public void destroy(TrinoCluster trinoCluster) {
        try {
            // delete coordinator.
            CoordinatorHandler coordinatorHandler = new CoordinatorHandler(trinoClusterClient.getClient());
            coordinatorHandler.delete(trinoCluster);
            LOG.info("coordinator deleted...");

            // delete workers.
            WorkerHandler workerHandler = new WorkerHandler(trinoClusterClient.getClient());
            workerHandler.delete(trinoCluster);
            LOG.info("worker deleted...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
