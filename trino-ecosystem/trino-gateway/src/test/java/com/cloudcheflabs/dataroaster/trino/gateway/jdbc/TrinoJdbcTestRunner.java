package com.cloudcheflabs.dataroaster.trino.gateway.jdbc;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
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
        Properties properties = new Properties();
        properties.setProperty("user", "trino");
        if(tls) {
            properties.setProperty("password", "trino123");
            properties.setProperty("SSL", "true");
        }
        Connection connection = DriverManager.getConnection(url, properties);
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM tpch.tiny.nation");

        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        while (rs.next()) {
            for(int count = 0; count < columnCount; count++) {
                LOG.info("[{}]: [{}]", meta.getColumnName(count + 1), rs.getObject(count + 1));
            }
            LOG.info("-------------------");
        }
    }
}
