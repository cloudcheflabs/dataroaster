package com.cloudcheflabs.dataroaster.apiserver.dao.common;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.Operations;
import com.google.common.base.Preconditions;

import java.io.Serializable;

public abstract class AbstractDao<T extends Serializable> implements Operations<T> {

    protected Class<T> clazz;

    protected final void setClazz(final Class<T> clazzToSet) {
        clazz = Preconditions.checkNotNull(clazzToSet);
    }
}
