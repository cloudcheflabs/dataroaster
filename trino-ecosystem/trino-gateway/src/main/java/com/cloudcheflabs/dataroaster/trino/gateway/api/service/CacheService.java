package com.cloudcheflabs.dataroaster.trino.gateway.api.service;

public interface CacheService<T> {

    void set(String id, T t);
    T get(String id, Class<T> clazz);
}
