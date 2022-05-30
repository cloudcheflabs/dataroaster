package com.cloudcheflabs.dataroaster.apiserver.api.dao;

public interface SecretDao<T> {
    void writeSecret(String path, Object value);
    T readSecret(String path, Class<T> clazz);
    void delete(String path);
}
