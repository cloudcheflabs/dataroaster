package com.cloudcheflabs.dataroaster.cli;

import com.cloudcheflabs.dataroaster.cli.api.dao.ClusterDao;
import com.cloudcheflabs.dataroaster.cli.api.dao.PodLogMonitoringDao;
import com.cloudcheflabs.dataroaster.cli.api.dao.ProjectDao;
import com.cloudcheflabs.dataroaster.cli.api.dao.ServicesDao;
import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.cli.domain.ServiceDef;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

public class PodLogMonitoringDaoTestRunner {

    @Test
    public void createPodLogMonitoring() throws Exception {

        String elasticsearchHosts = "192.168.10.10:9200,192.168.10.134:9200,192.168.10.145:9200";

        ConfigProps configProps = DataRoasterConfig.getConfigProps();

        java.io.Console cnsl = System.console();

        if (cnsl == null) {
            System.out.println("No console available");
            return;
        }

        // show project list.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ProjectDao projectDao = applicationContext.getBean(ProjectDao.class);
        RestResponse restResponse = projectDao.listProjects(configProps);
        List<Map<String, Object>> projectLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        String format = "%-20s%-20s%-20s%n";

        System.out.printf(format,"PROJECT ID", "PROJECT NAME", "PROJECT DESCRIPTION");
        for(Map<String, Object> map : projectLists) {
            System.out.printf(format, String.valueOf(map.get("id")), (String) map.get("name"), (String) map.get("description"));
        }

        String projectId = cnsl.readLine("Select Project : ");


        // show cluster list.
        ClusterDao clusterDao = applicationContext.getBean(ClusterDao.class);
        restResponse = clusterDao.listClusters(configProps);
        List<Map<String, Object>> clusterLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        System.out.printf(format,"CLUSTER ID", "CLUSTER NAME", "CLUSTER DESCRIPTION");
        for(Map<String, Object> map : clusterLists) {
            System.out.printf(format, String.valueOf(map.get("id")), (String) map.get("name"), (String) map.get("description"));
        }

        String clusterId = cnsl.readLine("Select Cluster : ");

        // get service def id.
        String serviceDefId = null;
        ServicesDao serviceDefDao = applicationContext.getBean(ServicesDao.class);
        restResponse = serviceDefDao.listServiceDef(configProps);
        List<Map<String, Object>> serviceDefLists =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());
        for(Map<String, Object> map : serviceDefLists) {
            String type = (String) map.get("type");
            if(type.equals(ServiceDef.ServiceTypeEnum.POD_LOG_MONITORING.name())) {
                serviceDefId = String.valueOf(map.get("id"));
                break;
            }
        }

        // create pod log monitoring.
        PodLogMonitoringDao podLogMonitoringDao = applicationContext.getBean(PodLogMonitoringDao.class);
        restResponse = podLogMonitoringDao.createPodLogMonitoring(configProps,
                Long.valueOf(projectId),
                Long.valueOf(serviceDefId),
                Long.valueOf(clusterId),
                elasticsearchHosts);
    }


    @Test
    public void deletePodLogMonitoring() throws Exception {
        ConfigProps configProps = DataRoasterConfig.getConfigProps();

        java.io.Console cnsl = System.console();

        if (cnsl == null) {
            System.out.println("No console available");
            return;
        }

        // show services list.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ServicesDao servicesDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = servicesDao.listServices(configProps);
        List<Map<String, Object>> servicesList =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        String format = "%-20s%-20s%-20s%-20s%n";

        System.out.printf(format,"SERVICE ID", "SERVICE TYPE", "CLUSTER NAME", "PROJECT NAME");
        for(Map<String, Object> map : servicesList) {
            String serviceType = (String) map.get("serviceDefType");
            if(serviceType.equals(ServiceDef.ServiceTypeEnum.POD_LOG_MONITORING.name())) {
                System.out.printf(format,
                        String.valueOf(map.get("id")),
                        (String) map.get("serviceDefType"),
                        (String) map.get("clusterName"),
                        (String) map.get("projectName"));
            }
        }

        String serviceId = cnsl.readLine("Select Service ID to be deleted : ");

        // delete pod log monitoring.
        PodLogMonitoringDao podLogMonitoringDao = applicationContext.getBean(PodLogMonitoringDao.class);
        restResponse = podLogMonitoringDao.deletePodLogMonitoring(configProps, Long.valueOf(serviceId));
    }
}
