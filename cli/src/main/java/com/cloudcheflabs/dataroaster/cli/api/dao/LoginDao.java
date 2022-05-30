package com.cloudcheflabs.dataroaster.cli.api.dao;

import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;

public interface LoginDao {
    RestResponse login(String user, String password, String serverUrl);
}
