package com.cloudcheflabs.dataroaster.apiserver.dao.hibernate;


import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sNamespaceDao;
import com.cloudcheflabs.dataroaster.apiserver.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sNamespace;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class HibernateK8sNamespaceDao extends AbstractHibernateDao<K8sNamespace> implements K8sNamespaceDao {

    public HibernateK8sNamespaceDao() {
        super();
        setClazz(K8sNamespace.class);
    }

    @Override
    public K8sNamespace findByNameAndClusterId(String namespaceName, long clusterId) {
        Query<K8sNamespace> query =
                this.getCurrentSession()
                        .createQuery("from " + clazz.getName() + " where namespace_name = :namespaceName and cluster_id = :clusterId", K8sNamespace.class);
        query.setParameter("namespaceName", namespaceName);
        query.setParameter("clusterId", clusterId);
        List<K8sNamespace> list = query.list();

        return (list.size() == 0) ? null : list.get(0);
    }
}
