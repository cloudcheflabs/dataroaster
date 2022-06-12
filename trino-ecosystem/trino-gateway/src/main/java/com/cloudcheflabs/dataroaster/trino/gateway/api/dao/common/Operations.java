package com.cloudcheflabs.dataroaster.trino.gateway.api.dao.common;

import java.io.Serializable;
import java.util.List;

public interface Operations<T extends Serializable> {

    T findOne(final String id);

    List<T> findAll();

    void create(final T entity);

    T update(final T entity);

    void delete(final T entity);

    void deleteById(final String entityId);

}
