package com.cloudcheflabs.dataroaster.operators.dataroaster.dao.kubernetes;

import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.K8sResourceDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.dao.common.AbstractKubernetesDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;
import com.cloudcheflabs.dataroaster.operators.dataroaster.service.UserTokenServiceImpl;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import jdk.internal.org.jline.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class KubernetesK8sResourceDao extends AbstractKubernetesDao implements K8sResourceDao {

    private static Logger LOG = LoggerFactory.getLogger(KubernetesK8sResourceDao.class);

    @Override
    public void createCustomResource(CustomResource customResource) {
        try {
            CustomResourceDefinitionList crds = kubernetesClient.apiextensions().v1().customResourceDefinitions().list();
            List<CustomResourceDefinition> crdsItems = crds.getItems();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteCustomResource(String name, String namespace) {
        try {
            // TODO
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateCustomResource(CustomResource customResource) {
        try {
            // TODO
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
