package com.cloudcheflabs.dataroaster.apiserver.service.common;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractService<T extends Serializable> implements Operations<T> {

    @Override
    public T findOne(final long id) {
        return getDao().findOne(id);
    }

    @Override
    public List<T> findAll() {
        return getDao().findAll();
    }

    @Override
    public void create(final T entity) {
        getDao().create(entity);
    }

    @Override
    public T update(final T entity) {
        return getDao().update(entity);
    }

    @Override
    public void delete(final T entity) {
        getDao().delete(entity);
    }

    @Override
    public void deleteById(final long entityId) {
        getDao().deleteById(entityId);
    }

    protected abstract Operations<T> getDao();

}
