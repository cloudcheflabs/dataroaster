package com.cloudcheflabs.dataroaster.operators.trino.handler;

public interface ActionHandler<T> {
    void submit(T t);
    void destroy(T t);
}
