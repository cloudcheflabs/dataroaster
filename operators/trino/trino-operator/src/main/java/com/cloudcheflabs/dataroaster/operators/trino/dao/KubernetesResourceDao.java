package com.cloudcheflabs.dataroaster.operators.trino.dao;

import com.cloudcheflabs.dataroaster.operators.trino.api.dao.ResourceDao;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;

import java.util.Map;

public class KubernetesResourceDao extends AbstractKubernetesResourceDao implements ResourceDao {

    public KubernetesResourceDao(KubernetesClient client) {
        super(client);
    }

    @Override
    public Map<String, String> getSecret(String namespace, String secretName) {
        Resource<io.fabric8.kubernetes.api.model.Secret> secret = client.secrets().inNamespace(namespace).withName(secretName);
        return secret.get().getData();
    }
}
