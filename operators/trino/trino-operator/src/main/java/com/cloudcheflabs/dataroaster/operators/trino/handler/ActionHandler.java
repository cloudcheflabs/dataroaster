package com.cloudcheflabs.dataroaster.operators.trino.handler;

public interface ActionHandler<T> {
    void create(T t);

    void update(T t);
    void destroy(T t);
}
