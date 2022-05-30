package com.cloudcheflabs.dataroaster.apiserver.service;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sClusterDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.SecretDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.UsersDao;
import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.cloudcheflabs.dataroaster.apiserver.api.service.K8sClusterService;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sKubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Users;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.YamlUtils;
import com.cloudcheflabs.dataroaster.apiserver.secret.SecretPathTemplate;
import com.cloudcheflabs.dataroaster.apiserver.service.common.AbstractHibernateService;
import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class K8sClusterServiceImpl extends AbstractHibernateService<K8sCluster> implements K8sClusterService {

    private static Logger LOG = LoggerFactory.getLogger(K8sClusterServiceImpl.class);

    @Autowired
    private K8sClusterDao dao;

    @Autowired
    @Qualifier("vaultKubeconfigSecretDao")
    private SecretDao<Kubeconfig> secretDao;

    @Autowired
    @Qualifier("hibernateUsersDao")
    private UsersDao usersDao;

    public K8sClusterServiceImpl() {
        super();
    }

    @Override
    protected Operations<K8sCluster> getDao() {
        return dao;
    }

    @Override
    public void createCluster(String clusterName, String description) {
        K8sCluster newCluster = new K8sCluster();
        newCluster.setClusterName(clusterName);
        newCluster.setDescription(description);

        dao.create(newCluster);
    }

    @Override
    public void updateCluster(long id, String clusterName, String description) {
        K8sCluster k8sCluster = dao.findOne(id);
        k8sCluster.setClusterName(clusterName);
        k8sCluster.setDescription(description);

        // update cluster.
        dao.update(k8sCluster);
    }

    @Override
    public void deleteCluster(long id) {
        K8sCluster k8sCluster = dao.findOne(id);

        k8sCluster.getK8sKubeconfigSet().forEach(k -> {
            String path = k.getSecretPath();

            // delete secret.
            secretDao.delete(path);
        });

        // delete cluster.
        dao.delete(k8sCluster);
    }

    @Override
    public void createKubeconfig(long id, String kubeconfig, String userName) {
        K8sCluster k8sCluster = dao.findOne(id);

        // get user.
        Users users = usersDao.findByUserName(userName);

        // kubeconfig yaml.
        Kubeconfig value = YamlUtils.readKubeconfigYaml(kubeconfig);
        Map<String, String> kv = new HashMap<>();
        kv.put("clusterId", String.valueOf(k8sCluster.getId()));
        kv.put("user", users.getUserName());
        String path = TemplateUtils.replace(SecretPathTemplate.SECRET_KUBECONFIG, kv);
        LOG.debug("secret path: {}", path);

        // add secret for kubeconfig.
        secretDao.writeSecret(path, value);

        // update cluster with secret path.
        K8sKubeconfig k8sKubeconfig = new K8sKubeconfig();
        k8sKubeconfig.setSecretPath(path);
        k8sKubeconfig.setUsers(users);
        k8sKubeconfig.setK8sCluster(k8sCluster);

        k8sCluster.getK8sKubeconfigSet().add(k8sKubeconfig);

        // update cluster.
        dao.update(k8sCluster);
    }

    @Override
    public void updateKubeconfig(long id, String kubeconfig, String userName) {
        K8sCluster k8sCluster = dao.findOne(id);

        // get user.
        Users users = usersDao.findByUserName(userName);

        // kubeconfig yaml.
        Kubeconfig value = YamlUtils.readKubeconfigYaml(kubeconfig);
        Map<String, String> kv = new HashMap<>();
        kv.put("clusterId", String.valueOf(k8sCluster.getId()));
        kv.put("user", users.getUserName());
        String path = TemplateUtils.replace(SecretPathTemplate.SECRET_KUBECONFIG, kv);
        LOG.debug("secret path: {}", path);

        // update secret for kubeconfig.
        secretDao.writeSecret(path, value);
    }

    @Override
    public Kubeconfig getKubeconfig(long clusterId, String userName) {
        // cluster.
        K8sCluster k8sCluster = dao.findOne(clusterId);

        // get user.
        Users users = usersDao.findByUserName(userName);

        // build secret path.
        Map<String, String> kv = new HashMap<>();
        kv.put("clusterId", String.valueOf(k8sCluster.getId()));
        kv.put("user", users.getUserName());
        String path = TemplateUtils.replace(SecretPathTemplate.SECRET_KUBECONFIG, kv);

        return secretDao.readSecret(path, Kubeconfig.class);
    }
}
