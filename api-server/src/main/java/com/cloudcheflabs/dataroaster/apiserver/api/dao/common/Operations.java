package com.cloudcheflabs.dataroaster.apiserver.api.dao.common;

import java.io.Serializable;
import java.util.List;

public interface Operations<T extends Serializable> {

    T findOne(final long id);

    List<T> findAll();

    void create(final T entity);

    T update(final T entity);

    void delete(final T entity);

    void deleteById(final long entityId);

}
