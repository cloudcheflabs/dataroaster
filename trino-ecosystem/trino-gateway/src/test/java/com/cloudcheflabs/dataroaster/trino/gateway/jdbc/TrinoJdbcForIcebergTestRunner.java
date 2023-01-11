package com.cloudcheflabs.dataroaster.trino.gateway.jdbc;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;

public class TrinoJdbcForIcebergTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(TrinoJdbcForIcebergTestRunner.class);

    @Test
    public void connect() throws Exception {
        String host = System.getProperty("host", "trino-gateway-proxy-test.cloudchef-labs.com");
        LOG.info("host: [{}]", host);
        String port = System.getProperty("port", "443");
        LOG.info("port: [{}]", port);
        boolean auth = Boolean.valueOf(System.getProperty("auth", "true"));
        LOG.info("auth: [{}]", auth);
        String query = System.getProperty("query", "select * from iceberg.iceberg_db.test_ctas limit 1000");
        LOG.info("query: [{}]", query);

        String url = "jdbc:trino://" + host + ":" + port + "/iceberg/iceberg_db";
        Properties properties = new Properties();
        properties.setProperty("user", "trino");
        if(auth) {
            properties.setProperty("password", "trino123");
            properties.setProperty("SSL", "true");
        }
        Connection connection = DriverManager.getConnection(url, properties);
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        while (rs.next()) {
            for(int count = 0; count < columnCount; count++) {
                LOG.info("{}: {}", meta.getColumnName(count + 1), rs.getObject(count + 1));
            }
        }
    }
}
