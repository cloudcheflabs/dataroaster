package com.cloudcheflabs.dataroaster.operators.dataroaster.component;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;

public class DBSchemaCreatorTestRunner {

    @Test
    public void createSchema() throws Exception {
        //Registering the Driver
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());

        //Getting the connection
        String mysqlUrl = "jdbc:mysql://localhost:3306/dataroaster?createDatabaseIfNotExist=true&useSSL=false";

        Connection con = DriverManager.getConnection(mysqlUrl, "root", "mysqlpass123");
        System.out.println("Connection established......");
        
        //Initialize the script runner
        ScriptRunner sr = new ScriptRunner(con);
        //Creating a reader object
        Reader reader = new BufferedReader(new FileReader("/opt/dataroaster-operator/create-tables.sql"));
        //Running the script
        sr.runScript(reader);
    }
}
