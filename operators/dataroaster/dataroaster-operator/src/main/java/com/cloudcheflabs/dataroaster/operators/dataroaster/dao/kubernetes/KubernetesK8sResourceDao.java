package com.cloudcheflabs.dataroaster.operators.dataroaster.dao.kubernetes;

import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.K8sResourceDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.dao.common.AbstractKubernetesDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionSpec;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Repository
@Transactional
public class KubernetesK8sResourceDao extends AbstractKubernetesDao implements K8sResourceDao {

    private static Logger LOG = LoggerFactory.getLogger(KubernetesK8sResourceDao.class);

    @Override
    public void createCustomResource(CustomResource customResource) {
        try {
            String name = customResource.getName();
            String namespace = customResource.getNamespace();
            String kind = customResource.getKind();
            String yaml = customResource.getYaml();

            CustomResourceDefinition selectedCRD = selectCRD(kind);
            if(selectedCRD == null) {
                throw new IllegalStateException("CRD with kind [" + kind + "] not found!");
            }

            InputStream is = new ByteArrayInputStream(yaml.getBytes());

            GenericKubernetesResource genericKubernetesResource =
                    kubernetesClient.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(selectedCRD))
                    .load(is).get();

            GenericKubernetesResource retResource =
                    kubernetesClient.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(selectedCRD))
                            .inNamespace(namespace)
                            .createOrReplace(genericKubernetesResource);
            if(retResource != null) {
                ObjectMeta meta = retResource.getMetadata();
                LOG.info("custom resource with name [{}] in namespace [{}] created or replaced", meta.getName(), meta.getNamespace());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private CustomResourceDefinition selectCRD(String kind) {
        CustomResourceDefinitionList crds = kubernetesClient.apiextensions().v1().customResourceDefinitions().list();
        List<CustomResourceDefinition> crdsItems = crds.getItems();
        LOG.info("[{}] CRDs found", crdsItems.size());

        CustomResourceDefinition selectedCRD = null;
        for (CustomResourceDefinition crd : crdsItems) {
            CustomResourceDefinitionSpec spec = crd.getSpec();
            String group = spec.getGroup();
            String crdKind = spec.getNames().getKind();
            LOG.info("group: [{}], crdKind: [{}]", group, crdKind);

            if(group.contains("cloudchef-labs.com") && crdKind.equals(kind)) {
                selectedCRD = crd;
                break;
            }
        }

        return selectedCRD;
    }

    @Override
    public void deleteCustomResource(String name, String namespace, String kind) {
        try {
            CustomResourceDefinition selectedCRD = selectCRD(kind);
            if(selectedCRD == null) {
                throw new IllegalStateException("CRD with kind [" + kind + "] not found!");
            }

            boolean result =
                    kubernetesClient.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(selectedCRD))
                            .inNamespace(namespace)
                            .withName(name).delete();
            LOG.info("custom resource - name: [{}], namespace: [{}], kind: [{}] deleted [{}]", name, namespace, kind, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateCustomResource(CustomResource customResource) {
        createCustomResource(customResource);
    }
}
