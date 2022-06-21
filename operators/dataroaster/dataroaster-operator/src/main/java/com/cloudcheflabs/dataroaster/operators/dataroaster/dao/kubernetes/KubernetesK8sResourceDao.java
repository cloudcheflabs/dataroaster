package com.cloudcheflabs.dataroaster.operators.dataroaster.dao.kubernetes;

import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.K8sResourceDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.dao.common.AbstractKubernetesDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class KubernetesK8sResourceDao extends AbstractKubernetesDao implements K8sResourceDao {

    @Override
    public void createCustomResource(CustomResource customResource) {
        CustomResourceDefinitionList crds = kubernetesClient.apiextensions().v1().customResourceDefinitions().list();
        List<CustomResourceDefinition> crdsItems = crds.getItems();
    }

    @Override
    public void deleteCustomResource(String name, String namespace) {

    }

    @Override
    public void updateCustomResource(CustomResource customResource) {

    }
}
