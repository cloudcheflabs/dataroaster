package com.cloudcheflabs.dataroaster.operators.dataroaster.dao.hibernate;


import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.CustomResourceDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class HibernateCustomResourceDao extends AbstractHibernateDao<CustomResource> implements CustomResourceDao {

    public HibernateCustomResourceDao() {
        super();
        setClazz(CustomResource.class);
    }

    @Override
    public CustomResource findCustomResource(String name, String namespace, String kind) {
        List<CustomResource> list = getCurrentSession()
                .createQuery("from " + clazz.getName() + " where name = :name and namespace = :namespace and kind = :kind")
                .setParameter("name", name)
                .setParameter("namespace", namespace)
                .setParameter("kind", kind).list();

        return (list.size() > 0) ? list.get(0) : null;
    }
}
