package com.cloudcheflabs.dataroaster.apiserver.dao.hibernate;


import com.cloudcheflabs.dataroaster.apiserver.api.dao.ServicesDao;
import com.cloudcheflabs.dataroaster.apiserver.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Services;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateServicesDao extends AbstractHibernateDao<Services> implements ServicesDao {

    public HibernateServicesDao() {
        super();
        setClazz(Services.class);
    }
}
