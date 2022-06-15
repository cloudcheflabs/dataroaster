package com.cloudcheflabs.dataroaster.trino.gateway.jdbc;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class TrinoJdbcTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(TrinoJdbcTestRunner.class);

    @Test
    public void connect() throws Exception {
        String host = System.getProperty("host", "trino-gateway-proxy-test.cloudchef-labs.com:443");
        LOG.info("host: [{}]", host);
        boolean tls = Boolean.valueOf(System.getProperty("tls", "true"));
        LOG.info("tls: [{}]", tls);

        String url = "jdbc:trino://" + host + "/tpch/tiny";
        Connection connection = null;
        if(tls) {
            Properties properties = new Properties();
            properties.setProperty("user", "trino");
            properties.setProperty("password", "trino123");
            properties.setProperty("SSL", "true");
            connection = DriverManager.getConnection(url, properties);
        } else {
            url = "jdbc:trino://" + host + "/tpch/tiny?SSL=false";
            connection = DriverManager.getConnection(url);
        }
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT count(*) FROM tpch.tiny.nation");

        while (rs.next()) {
            LOG.info("nationkey: " + rs.getInt("nationkey"));
            LOG.info(", name: " + rs.getString("name"));
            LOG.info(", regionkey: " + rs.getInt("regionkey"));
            LOG.info(", comment: " + rs.getString("comment"));
        }
    }
}
