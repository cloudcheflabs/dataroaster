package com.cloudcheflabs.dataroaster.hive;

public class SparkThriftServerRunner {

    public static void main(String[] args) {
        org.apache.spark.sql.hive.thriftserver.HiveThriftServer2.main(args);

        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
