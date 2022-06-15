package com.cloudcheflabs.dataroaster.trino.gateway.dao.hibernate;


import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.ClusterGroupDao;
import com.cloudcheflabs.dataroaster.trino.gateway.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.ClusterGroup;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateClusterGroupDao extends AbstractHibernateDao<ClusterGroup> implements ClusterGroupDao {

    public HibernateClusterGroupDao() {
        super();
        setClazz(ClusterGroup.class);
    }
}
