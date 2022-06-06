package com.cloudcheflabs.dataroaster.operators.trino.api.dao;

import java.util.Map;

public interface ResourceDao {
    Map<String, String> getSecret(String namespace, String secretName);
}
