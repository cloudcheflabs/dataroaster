package com.cloudcheflabs.dataroaster.operators.trino.dao;

import com.cloudcheflabs.dataroaster.operators.trino.api.dao.K8sResourceDao;
import com.cloudcheflabs.dataroaster.operators.trino.domain.CustomResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;


public class KubernetesK8sResourceDao extends AbstractKubernetesResourceDao implements K8sResourceDao {

    private static Logger LOG = LoggerFactory.getLogger(KubernetesK8sResourceDao.class);

    public KubernetesK8sResourceDao(KubernetesClient client) {
        super(client);
    }

    @Override
    public void createCustomResource(CustomResource customResource) {
        try {
            String namespace = customResource.getNamespace();
            //LOG.info("namespace: {}", namespace);

            String kind = customResource.getKind();
            //LOG.info("kind: {}", kind);

            String yaml = customResource.getYaml();
            //LOG.info("yaml: \n{}", yaml);

            CustomResourceDefinition selectedCRD = selectCRD(kind);
            if(selectedCRD == null) {
                throw new IllegalStateException("CRD with kind [" + kind + "] not found!");
            }

            InputStream is = new ByteArrayInputStream(yaml.getBytes());

            GenericKubernetesResource genericKubernetesResource =
                    client.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(selectedCRD))
                    .load(is).get();

            GenericKubernetesResource retResource = (namespace != null) ?
                    client.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(selectedCRD))
                            .inNamespace(namespace)
                            .createOrReplace(genericKubernetesResource) :
                    client.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(selectedCRD))
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

    @Override
    public List<GenericKubernetesResource> listCustomResources(String namespace, String kind) {
        CustomResourceDefinition selectedCRD = selectCRD(kind);
        if(selectedCRD == null) {
            throw new IllegalStateException("CRD with kind [" + kind + "] not found!");
        }

        GenericKubernetesResourceList genericKubernetesResourceList =
                client.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(selectedCRD))
                        .inNamespace(namespace).list();
        return genericKubernetesResourceList.getItems();
    }

    private CustomResourceDefinition selectCRD(String kind) {
        CustomResourceDefinitionList crds = client.apiextensions().v1().customResourceDefinitions().list();
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

        // if there is no crd selected, search for crd outside group of cloudchef-labs.com.
        if(selectedCRD == null) {
            for (CustomResourceDefinition crd : crdsItems) {
                CustomResourceDefinitionSpec spec = crd.getSpec();
                String group = spec.getGroup();
                String crdKind = spec.getNames().getKind();
                LOG.info("group: [{}], crdKind: [{}]", group, crdKind);

                if(crdKind.equals(kind)) {
                    selectedCRD = crd;
                    break;
                }
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

            boolean result = (namespace != null) ?
                    client.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(selectedCRD))
                            .inNamespace(namespace)
                            .withName(name).delete() :
                    client.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(selectedCRD))
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

    @Override
    public void updateCustomResource(GenericKubernetesResource genericKubernetesResource) {
        String kind = genericKubernetesResource.getKind();
        ObjectMeta meta = genericKubernetesResource.getMetadata();
        String name = meta.getName();
        String namespace = meta.getNamespace();

        CustomResourceDefinition selectedCRD = selectCRD(kind);
        if(selectedCRD == null) {
            throw new IllegalStateException("CRD with kind [" + kind + "] not found!");
        }

        GenericKubernetesResource retResource =
                client.genericKubernetesResources(CustomResourceDefinitionContext.fromCrd(selectedCRD))
                        .inNamespace(namespace)
                        .withName(name).createOrReplace(genericKubernetesResource);

        if(retResource != null) {
            LOG.info("custom resource with name [{}] in namespace [{}] created or replaced", meta.getName(), meta.getNamespace());
        }
    }
}
