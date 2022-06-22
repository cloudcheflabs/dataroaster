package com.cloudcheflabs.dataroaster.operators.dataroaster.dao.hibernate;


import com.cloudcheflabs.dataroaster.operators.dataroaster.api.dao.UsersDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.Users;
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
