package com.cloudcheflabs.dataroaster.apiserver.service.common;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Transactional
public abstract class AbstractHibernateService<T extends Serializable> extends AbstractService<T> implements Operations<T> {

    @Override
    @Transactional
    public T findOne(final long id) {
        return super.findOne(id);
    }

    @Override
    @Transactional
    public List<T> findAll() {
        return super.findAll();
    }

    @Override
    @Transactional
    public void create(final T entity) {
        super.create(entity);
    }

    @Override
    @Transactional
    public T update(final T entity) {
        return super.update(entity);
    }

    @Override
    @Transactional
    public void delete(final T entity) {
        super.delete(entity);
    }

    @Override
    @Transactional
    public void deleteById(final long entityId) {
        super.deleteById(entityId);
    }

}
