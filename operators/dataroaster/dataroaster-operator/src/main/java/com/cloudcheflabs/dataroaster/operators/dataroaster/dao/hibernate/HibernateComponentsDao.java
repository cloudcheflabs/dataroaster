package com.cloudcheflabs.dataroaster.operators.dataroaster.dao.hibernate;


import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.ComponentsDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.Components;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateComponentsDao extends AbstractHibernateDao<Components> implements ComponentsDao {

    public HibernateComponentsDao() {
        super();
        setClazz(Components.class);
    }
}
