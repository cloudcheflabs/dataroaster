package com.cloudcheflabs.dataroaster.apiserver.dao.hibernate;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.UserAuthoritiesDao;
import com.cloudcheflabs.dataroaster.apiserver.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.UserAuthorities;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateUserAuthoritiesDao extends AbstractHibernateDao<UserAuthorities> implements UserAuthoritiesDao {

    public HibernateUserAuthoritiesDao() {
        super();
        setClazz(UserAuthorities.class);
    }
}
