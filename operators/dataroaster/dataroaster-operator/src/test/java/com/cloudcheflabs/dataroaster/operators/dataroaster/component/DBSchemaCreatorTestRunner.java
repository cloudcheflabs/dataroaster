package com.cloudcheflabs.dataroaster.operators.dataroaster.component;

import org.junit.Test;

import java.util.Arrays;

public class DBSchemaCreatorTestRunner {

    @Test
    public void createSchema() throws Exception {
        // system property -DdataroasterKubeconfig=... must be set before running test.
        DBSchemaCreator.main(Arrays.asList("root", "mysqlpass123", "/opt/dataroaster-operator/create-tables.sql").toArray(new String[0]));
    }
}
