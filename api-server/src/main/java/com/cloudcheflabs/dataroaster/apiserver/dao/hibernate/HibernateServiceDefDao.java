package com.cloudcheflabs.dataroaster.apiserver.dao.hibernate;


import com.cloudcheflabs.dataroaster.apiserver.api.dao.ServiceDefDao;
import com.cloudcheflabs.dataroaster.apiserver.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.ServiceDef;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateServiceDefDao extends AbstractHibernateDao<ServiceDef> implements ServiceDefDao {

    public HibernateServiceDefDao() {
        super();
        setClazz(ServiceDef.class);
    }
}
