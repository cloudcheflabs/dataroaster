package com.cloudcheflabs.dataroaster.trino.gateway.component;

import com.cloudcheflabs.dataroaster.trino.gateway.api.service.CacheService;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.ClusterGroupService;
import com.cloudcheflabs.dataroaster.trino.gateway.api.service.TrinoActiveQueryCountRestService;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.TrinoActiveQueryCount;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Cluster;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.ClusterGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TrinoActiveQueryCountUpdater implements Runnable {

    private static Logger LOG = LoggerFactory.getLogger(TrinoActiveQueryCountUpdater.class);


    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("trinoActiveQueryCountRestServiceImpl")
    private TrinoActiveQueryCountRestService trinoActiveQueryCountRestService;

    @Autowired
    @Qualifier("trinoActiveQueryCountCacheServiceImpl")
    private CacheService<TrinoActiveQueryCount> trinoActiveQueryCountUpdaterCacheService;

    @Autowired
    @Qualifier("clusterGroupServiceImpl")
    private ClusterGroupService clusterGroupService;

    private String restUri;


    public TrinoActiveQueryCountUpdater() {
        new Thread(this).start();
    }


    @Override
    public void run() {

        while (true) {
            try {
                // get current cluster list.
                List<Cluster> clusterList = new ArrayList<>();
                List<ClusterGroup> clusterGroupList = clusterGroupService.findAll();
                if (clusterGroupList == null) {
                    pause(1000);
                    continue;
                }
                for (ClusterGroup clusterGroup : clusterGroupList) {
                    for (Cluster cluster : clusterGroup.getClusterSet()) {
                        if (cluster.isActivated()) {
                            clusterList.add(cluster);
                        }
                    }
                }

                if (clusterList.size() == 0) {
                    pause(1000);
                    continue;
                }
                for (Cluster cluster : clusterList) {
                    String clusterName = cluster.getClusterName();
                    // get active query count from trino operator using rest api.
                    TrinoActiveQueryCount trinoActiveQueryCount = trinoActiveQueryCountRestService.getTrinoActiveQueryCount(clusterName);
                    if (trinoActiveQueryCount == null) {
                        continue;
                    }
                    // update active query count to redis.
                    trinoActiveQueryCountUpdaterCacheService.set(clusterName, trinoActiveQueryCount);
                }
                pause(1000);
            } catch (Exception e) {
                e.printStackTrace();
                pause(5000);
                continue;
            }
        }

    }


    public static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
