package com.cloudcheflabs.dataroaster.trino.controller.dao.common;

import com.cloudcheflabs.dataroaster.trino.controller.component.SimpleHttpClient;
import okhttp3.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public abstract class AbstractRestDao {

    @Autowired
    protected SimpleHttpClient simpleHttpClient;

    protected MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
}
