package com.cloudcheflabs.dataroaster.apiserver.dao.hibernate;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.UsersDao;
import com.cloudcheflabs.dataroaster.apiserver.dao.common.AbstractHibernateDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.Users;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class HibernateUsersDao extends AbstractHibernateDao<Users> implements UsersDao {

    public HibernateUsersDao() {
        super();
        setClazz(Users.class);
    }

    @Override
    @Transactional
    public Users findByUserName(String userName) {
        Query<Users> query = this.getCurrentSession().createQuery("from " + clazz.getName() + " where userName = :userName", clazz);
        query.setParameter("userName", userName);

        List<Users> list = query.list();
        return (list.size() == 0) ? null : list.get(0);
    }
}
