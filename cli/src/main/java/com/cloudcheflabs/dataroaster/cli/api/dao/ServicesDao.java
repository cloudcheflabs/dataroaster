package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface ServicesDao {
    RestResponse listServiceDef(ConfigProps configProps);
    RestResponse listServices(ConfigProps configProps);
}
