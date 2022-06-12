package com.cloudcheflabs.dataroaster.trino.gateway.dao.hibernate;


import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.UsersDao;
import com.cloudcheflabs.dataroaster.trino.gateway.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.trino.gateway.domain.model.Users;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateUsersDao extends AbstractHibernateDao<Users> implements UsersDao {

    public HibernateUsersDao() {
        super();
        setClazz(Users.class);
    }
}
