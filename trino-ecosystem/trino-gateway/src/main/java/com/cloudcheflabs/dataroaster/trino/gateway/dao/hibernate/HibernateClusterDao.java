package com.cloudcheflabs.dataroaster.trino.gateway.dao.hibernate;


import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.ClusterDao;
import com.cloudcheflabs.dataroaster.trino.gateway.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Cluster;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateClusterDao extends AbstractHibernateDao<Cluster> implements ClusterDao {

    public HibernateClusterDao() {
        super();
        setClazz(Cluster.class);
    }
}
