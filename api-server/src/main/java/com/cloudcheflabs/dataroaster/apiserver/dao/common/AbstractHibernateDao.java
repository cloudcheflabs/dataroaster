package com.cloudcheflabs.dataroaster.apiserver.dao.common;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.google.common.base.Preconditions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unchecked")
@Transactional
public abstract class AbstractHibernateDao<T extends Serializable> extends AbstractDao<T>
        implements Operations<T> {

    @Autowired
    protected SessionFactory sessionFactory;

    // API

    @Override
    @Transactional
    public T findOne(final long id) {
        return (T) getCurrentSession().get(clazz, id);
    }

    @Override
    @Transactional
    public List<T> findAll() {
        return getCurrentSession().createQuery("from " + clazz.getName()).list();
    }

    @Override
    @Transactional
    public void create(final T entity) {
        Preconditions.checkNotNull(entity);
        getCurrentSession().saveOrUpdate(entity);
    }

    @Override
    @Transactional
    public T update(final T entity) {
        Preconditions.checkNotNull(entity);
        return (T) getCurrentSession().merge(entity);
    }

    @Override
    @Transactional
    public void delete(final T entity) {
        Preconditions.checkNotNull(entity);
        getCurrentSession().delete(entity);
    }

    @Override
    @Transactional
    public void deleteById(final long entityId) {
        final T entity = findOne(entityId);
        Preconditions.checkState(entity != null);
        delete(entity);
    }

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
}