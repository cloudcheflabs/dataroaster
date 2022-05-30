package com.cloudcheflabs.dataroaster.apiserver.api.service;

public interface SecretService<T> {
    void writeSecret(String path, Object value);
    T readSecret(String path, Class<T> clazz);
    void delete(String path);
}
