package com.cloudcheflabs.dataroaster.cli.config;

import com.cloudcheflabs.dataroaster.cli.api.dao.*;
import com.cloudcheflabs.dataroaster.cli.dao.http.*;
import com.cloudcheflabs.dataroaster.cli.http.client.SimpleHttpClient;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class APIConfig {

    @Bean
    public LoginDao loginDao() {
        return new HttpLoginDao(httpClient());
    }

    @Bean
    public ClusterDao clusterDao() {
        return new HttpClusterDao(httpClient());
    }

    @Bean
    public KubeconfigDao kubeconfigDao() {
        return new HttpKubeconfigDao(httpClient());
    }

    @Bean
    public ProjectDao projectDao() {
        return new HttpProjectDao(httpClient());
    }

    @Bean
    public PodLogMonitoringDao podLogMonitoringDao() {
        return new HttpPodLogMonitoringDao(httpClient());
    }

    @Bean
    public ServicesDao servicesDao() {
        return new HttpServicesDao(httpClient());
    }

    @Bean
    public MetricsMonitoringDao metricsMonitoringDao() {
        return new HttpMetricsMonitoringDao(httpClient());
    }

    @Bean
    public ResourceControlDao resourceControlDao() {
        return new HttpResourceControlDao(httpClient());
    }

    @Bean
    public DistributedTracingDao distributedTracingDao() {
        return new HttpDistributedTracingDao(httpClient());
    }

    @Bean
    public PrivateRegistryDao privateRegistryDao() {
        return new HttpPrivateRegistryDao(httpClient());
    }

    @Bean
    public CiCdDao ciCdDao() {
        return new HttpCiCdDao(httpClient());
    }

    @Bean
    public BackupDao backupDao() {
        return new HttpBackupDao(httpClient());
    }

    @Bean
    public DataCatalogDao dataCatalogDao() { return new HttpDataCatalogDao(httpClient()); }

    @Bean
    public QueryEngineDao queryEngineDao() { return new HttpQueryEngineDao(httpClient()); }

    @Bean
    public StreamingDao streamingDao() { return new HttpStreamingDao(httpClient()); }

    @Bean
    public AnalyticsDao analyticsDao() { return new HttpAnalyticsDao(httpClient()); }

    @Bean
    public WorkflowDao workflowDao() { return new HttpWorkflowDao(httpClient()); }

    @Bean
    public IngressControllerDao ingressControllerDao() { return new HttpIngressControllerDao(httpClient()); }

    @Bean
    public OkHttpClient httpClient() {
        return new SimpleHttpClient().getClient();
    }
}
