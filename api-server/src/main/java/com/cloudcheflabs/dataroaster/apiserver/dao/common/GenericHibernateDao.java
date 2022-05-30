package com.cloudcheflabs.dataroaster.apiserver.dao.common;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.common.GenericDao;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GenericHibernateDao<T extends Serializable> extends AbstractHibernateDao<T> implements GenericDao<T> {
    //
}