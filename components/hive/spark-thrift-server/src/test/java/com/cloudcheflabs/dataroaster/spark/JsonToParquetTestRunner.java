package com.cloudcheflabs.dataroaster.spark;

import com.cloudcheflabs.dataroaster.util.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;

import java.util.Arrays;

public class JsonToParquetTestRunner {

    @Test
    public void jsonToParquet() throws Exception
    {
        String s3Bucket = System.getProperty("s3Bucket");
        String s3AccessKey = System.getProperty("s3AccessKey");
        String s3SecretKey = System.getProperty("s3SecretKey");
        String s3Endpoint = System.getProperty("s3Endpoint");
        String metastoreUrl = System.getProperty("metastoreUrl");

        SparkConf sparkConf = new SparkConf().setAppName("create sample table");
        sparkConf.setMaster("local[2]");

        SparkSession spark = SparkSession
                .builder()
                .config(sparkConf)
                .enableHiveSupport()
                .getOrCreate();


        Configuration hadoopConfiguration = spark.sparkContext().hadoopConfiguration();
        hadoopConfiguration.set("fs.defaultFS", "s3a://" + s3Bucket);
        hadoopConfiguration.set("fs.s3a.endpoint", s3Endpoint);
        hadoopConfiguration.set("fs.s3a.access.key", s3AccessKey);
        hadoopConfiguration.set("fs.s3a.secret.key", s3SecretKey);
        hadoopConfiguration.set("fs.s3a.path.style.access", "true");
        hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        hadoopConfiguration.set("hive.metastore.uris", "thrift://" + metastoreUrl);
        hadoopConfiguration.set("hive.server2.enable.doAs", "false");
        hadoopConfiguration.set("hive.metastore.client.socket.timeout", "1800");
        hadoopConfiguration.set("hive.execution.engine", "spark");


        // read json.
        String json = StringUtils.fileToString("data/test.json", true);
        String lines[] = json.split("\\r?\\n");
        Dataset<Row> df = spark.read().json(new JavaSparkContext(spark.sparkContext()).parallelize(Arrays.asList(lines)));

        df.show(10);

        // create persistent parquet table with external path.
        df.write().format("parquet")
                .option("path", "s3a://" + s3Bucket + "/test-parquet")
                .mode(SaveMode.Overwrite)
                .saveAsTable("test_parquet");

        // read parquet from table.
        Dataset<Row> parquet = spark.sql("select * from test_parquet");

        parquet.show(10);
    }
}
