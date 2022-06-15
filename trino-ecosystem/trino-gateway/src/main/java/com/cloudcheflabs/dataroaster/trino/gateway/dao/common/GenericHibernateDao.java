package com.cloudcheflabs.dataroaster.trino.gateway.dao.common;

import com.cloudcheflabs.dataroaster.trino.gateway.api.dao.common.GenericDao;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GenericHibernateDao<T extends Serializable> extends AbstractHibernateDao<T> implements GenericDao<T> {
    //
}