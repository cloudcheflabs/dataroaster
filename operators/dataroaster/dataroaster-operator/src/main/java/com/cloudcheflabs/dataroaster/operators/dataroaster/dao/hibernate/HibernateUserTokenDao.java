package com.cloudcheflabs.dataroaster.operators.dataroaster.dao.hibernate;


import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.UserTokenDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.UserToken;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateUserTokenDao extends AbstractHibernateDao<UserToken> implements UserTokenDao {

    public HibernateUserTokenDao() {
        super();
        setClazz(UserToken.class);
    }
}
