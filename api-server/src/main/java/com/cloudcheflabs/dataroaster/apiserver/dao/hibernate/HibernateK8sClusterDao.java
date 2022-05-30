package com.cloudcheflabs.dataroaster.apiserver.dao.hibernate;


import com.cloudcheflabs.dataroaster.apiserver.api.dao.K8sClusterDao;
import com.cloudcheflabs.dataroaster.apiserver.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sCluster;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class HibernateK8sClusterDao extends AbstractHibernateDao<K8sCluster> implements K8sClusterDao {

    public HibernateK8sClusterDao() {
        super();
        setClazz(K8sCluster.class);
    }

    @Override
    public K8sCluster findByName(String clusterName) {
        Query<K8sCluster> query = this.getCurrentSession().createQuery("from " + clazz.getName() + " where clusterName = :clusterName", clazz);
        query.setParameter("clusterName", clusterName);

        List<K8sCluster> list = query.list();
        return (list.size() == 0) ? null : list.get(0);
    }
}
