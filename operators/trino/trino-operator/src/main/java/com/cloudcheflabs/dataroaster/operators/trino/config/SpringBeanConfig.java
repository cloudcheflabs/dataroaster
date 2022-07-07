package com.cloudcheflabs.dataroaster.operators.trino.config;


import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.operators.trino.TrinoOperator;
import com.cloudcheflabs.dataroaster.operators.trino.api.dao.ResourceDao;
import com.cloudcheflabs.dataroaster.operators.trino.crd.TrinoCluster;
import com.cloudcheflabs.dataroaster.operators.trino.dao.KubernetesResourceDao;
import com.cloudcheflabs.dataroaster.operators.trino.handler.ActionHandler;
import com.cloudcheflabs.dataroaster.operators.trino.handler.TrinoClusterActionHandler;
import com.cloudcheflabs.dataroaster.operators.trino.handler.TrinoClusterClient;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringBeanConfig {

    public static final String PROPERTY_TRINO_OPERATOR_KUBECONFIG = "trinoOperatorKubeconfig";
    @Bean
    public ResourceDao resourceDao() { return new KubernetesResourceDao(kubernetesClient()); }

    @Bean
    public TrinoClusterClient trinoClusterClient() { return new TrinoClusterClient(kubernetesClient()); }

    @Bean
    public ActionHandler<TrinoCluster> actionHandler() {
        return new TrinoClusterActionHandler(trinoClusterClient());
    }

    @Bean
    public TrinoOperator trinoOperator() {
        return new TrinoOperator(trinoClusterClient(), actionHandler());
    }


    @Bean
    public KubernetesClient kubernetesClient() {
        String kubeconfig = System.getProperty(PROPERTY_TRINO_OPERATOR_KUBECONFIG);
        if(kubeconfig != null) {
            String kubeConfigYaml = FileUtils.fileToString(kubeconfig, false);
            try {
                Config config = Config.fromKubeconfig(kubeConfigYaml);
                return new DefaultKubernetesClient(config);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return new DefaultKubernetesClient();
        }
    }

}
