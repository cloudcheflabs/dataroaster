package com.cloudcheflabs.dataroaster.cli;

import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.cli.api.dao.ClusterDao;
import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

public class ClusterDaoTestRunner {

    @Test
    public void createCluster() throws Exception {
        ConfigProps configProps = DataRoasterConfig.getConfigProps();
        System.out.println("access token: [" + configProps.getAccessToken() + "]");

        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);

        String name = "new-test-cluster-name";
        String description = "new-test-cluster-description";
        RestResponse restResponse = clusterDao.createCluster(configProps, name, description);
    }

    @Test
    public void updateCluster() throws Exception {
        ConfigProps configProps = DataRoasterConfig.getConfigProps();

        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);

        long id = Long.valueOf(System.getProperty("id"));

        String name = "updated-new-test-cluster-name";
        String description = "updated-new-test-cluster-description";
        RestResponse restResponse = clusterDao.updateCluster(configProps, id, name, description);
    }

    @Test
    public void deleteCluster() throws Exception {
        ConfigProps configProps = DataRoasterConfig.getConfigProps();

        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);

        long id = Long.valueOf(System.getProperty("id"));

        RestResponse restResponse = clusterDao.deleteCluster(configProps, id);
    }

    @Test
    public void listClusters() throws Exception {
        ConfigProps configProps = DataRoasterConfig.getConfigProps();

        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);

        RestResponse restResponse = clusterDao.listClusters(configProps);

        System.out.printf("response: \n%s\n", JsonWriter.formatJson(restResponse.getSuccessMessage()));

        List<Map<String, Object>> clusterLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        String format = "%-20s%-20s%-20s%n";

        System.out.printf(format,"CLUSTER ID", "CLUSTER NAME", "CLUSTER DESCRIPTION");
        for(Map<String, Object> map : clusterLists) {
            System.out.printf(format, String.valueOf(map.get("id")), (String) map.get("name"), (String) map.get("description"));
        }
    }
}
