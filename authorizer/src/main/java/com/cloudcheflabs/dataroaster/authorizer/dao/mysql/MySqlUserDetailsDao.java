package com.cloudcheflabs.dataroaster.authorizer.dao.mysql;

import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.authorizer.api.dao.UserDetailsDao;
import com.cloudcheflabs.dataroaster.authorizer.domain.UserInfo;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class MySqlUserDetailsDao implements UserDetailsDao {

    private static Logger LOG = LoggerFactory.getLogger(MySqlUserDetailsDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) {

        List<String> roles = this.getRoles(username);

        String query = "SELECT username, password, enabled FROM users WHERE username = '" + username + "'";
        List<Map<String, Object>> retList = jdbcTemplate.queryForList(query);
        Map<String, Object> map = retList.get(0);
        String userName = (String) map.get("username");
        String password = (String) map.get("password");
        boolean enabled = (Boolean) map.get("enabled");

        UserInfo userInfo = new UserInfo(userName, password, enabled, roles);

        LOG.debug("user details: {}", JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), userInfo)));

        return userInfo;
    }

    @Override
    public List<String> getRoles(String userName) {

        String query = "";
        query += "SELECT ";
        query += "  a.authority AS role ";
        query += "FROM ";
        query += "  user_authorities a, users u ";
        query += "WHERE ";
        query += "  u.username = '" + userName + "' AND u.id = a.user_id";

        List<Map<String, Object>> retList = jdbcTemplate.queryForList(query);

        List<String> roleList = new ArrayList<>();
        for(Map<String, Object> map : retList) {
            String role = (String) map.get("role");
            roleList.add(role);
        }

        return roleList;
    }
}
