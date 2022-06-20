package com.cloudcheflabs.dataroaster.operators.helm.handler;

public interface ActionHandler<T> {
    void create(T t);
    void upgrade(T t);
    void destroy(T t);
}
