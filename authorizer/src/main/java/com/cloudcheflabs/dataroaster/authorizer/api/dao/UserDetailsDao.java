package com.cloudcheflabs.dataroaster.authorizer.api.dao;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserDetailsDao {

    UserDetails loadUserByUsername(String username);

    List<String> getRoles(String userName);
}
