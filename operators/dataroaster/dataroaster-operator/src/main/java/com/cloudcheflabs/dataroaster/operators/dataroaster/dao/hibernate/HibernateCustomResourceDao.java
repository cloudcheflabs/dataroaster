package com.cloudcheflabs.dataroaster.operators.dataroaster.dao.hibernate;


import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.CustomResourceDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateCustomResourceDao extends AbstractHibernateDao<CustomResource> implements CustomResourceDao {

    public HibernateCustomResourceDao() {
        super();
        setClazz(CustomResource.class);
    }
}
