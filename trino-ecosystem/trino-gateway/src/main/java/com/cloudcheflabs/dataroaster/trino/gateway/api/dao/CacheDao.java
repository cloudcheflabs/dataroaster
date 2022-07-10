package com.cloudcheflabs.dataroaster.trino.gateway.api.dao;

public interface CacheDao<T> {

    void set(String id, T t);
    T get(String id, Class<T> clazz);
}
