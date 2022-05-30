package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sClusterDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.ResourceControlDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.SecretDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.UsersDao;
import com.cloudcheflabs.dataroaster.apiserver.api.service.ResourceControlService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.StorageClass;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sNamespace;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Users;
import com.cloudcheflabs.dataroaster.apiserver.secret.SecretPathTemplate;
import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ResourceControlServiceImpl implements ResourceControlService {

    private static Logger LOG = LoggerFactory.getLogger(ResourceControlServiceImpl.class);

    @Autowired
    @Qualifier("hibernateK8sClusterDao")
    private K8sClusterDao k8sClusterDao;

    @Autowired
    @Qualifier("vaultKubeconfigSecretDao")
    private SecretDao<Kubeconfig> secretDao;

    @Autowired
    @Qualifier("hibernateUsersDao")
    private UsersDao usersDao;

    @Autowired
    @Qualifier("kubernetesResourceControlDao")
    private ResourceControlDao resourceControlDao;

    @Override
    public List<StorageClass> listStorageClasses(long clusterId, String userName) {
        // cluster.
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);

        // get user.
        Users users = usersDao.findByUserName(userName);

        // build secret path.
        Map<String, String> kv = new HashMap<>();
        kv.put("clusterId", String.valueOf(k8sCluster.getId()));
        kv.put("user", users.getUserName());
        String path = TemplateUtils.replace(SecretPathTemplate.SECRET_KUBECONFIG, kv);
        Kubeconfig kubeconfig = secretDao.readSecret(path, Kubeconfig.class);

        return resourceControlDao.listStorageClasses(kubeconfig);
    }

    @Override
    public String getExternalIpOfIngressControllerNginx(long clusterId, String userName) {
        // cluster.
        K8sCluster k8sCluster = k8sClusterDao.findOne(clusterId);

        // get user.
        Users users = usersDao.findByUserName(userName);

        // build secret path.
        Map<String, String> kv = new HashMap<>();
        kv.put("clusterId", String.valueOf(k8sCluster.getId()));
        kv.put("user", users.getUserName());
        String path = TemplateUtils.replace(SecretPathTemplate.SECRET_KUBECONFIG, kv);
        Kubeconfig kubeconfig = secretDao.readSecret(path, Kubeconfig.class);

        return resourceControlDao.getExternalIpOfIngressControllerNginx(kubeconfig, K8sNamespace.DEFAULT_NAMESPACE_INGRESS_CONTROLLER_NGINX);
    }
}
