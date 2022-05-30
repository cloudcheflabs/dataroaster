package com.cloudcheflabs.dataroaster.authorizer.api.service;

import java.util.List;

public interface RoleService {

    List<String> getRoles(String userName);
}
