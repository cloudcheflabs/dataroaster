package com.cloudcheflabs.dataroaster.operators.trino.handler;

import com.cloudcheflabs.dataroaster.operators.trino.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;

public class TrinoClusterActionHandler implements ActionHandler<TrinoCluster> {

    private TrinoClusterClient trinoClusterClient;

    public TrinoClusterActionHandler() {
        trinoClusterClient = SpringContextSingleton.getInstance().getBean(TrinoClusterClient.class);
    }


    @Override
    public void submit(TrinoCluster trinoCluster) {
        // TODO: create configmaps, coordinator deployment, worker deployment for trino cluster in the specified namespace.
    }

    @Override
    public void destroy(TrinoCluster trinoCluster) {
        // TODO: delete all the resources deployed in the specified trino namespace.
    }
}
