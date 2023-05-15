package com.cloudcheflabs.dataroaster.operators.spark.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class S3Utils {

    private static Logger LOG = LoggerFactory.getLogger(S3Utils.class);

    public static void downloadObject(String accessKey, String secretKey, String endpoint, String region, String s3Path, String toFilePath) {
        s3Path = s3Path.replaceAll("s3a://", "");
        int index = s3Path.indexOf("/");
        String s3Bucket = s3Path.substring(0, index);
        String objectPath = s3Path.substring(index + 1, s3Path.length());

        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSignerOverride("AWSS3V4SignerType");

        AmazonS3 s3Client = AmazonS3ClientBuilder
                .standard()
                .withRegion(region)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, null))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        S3Object s3Object = s3Client.getObject(s3Bucket, objectPath);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            FileUtils.copyInputStreamToFile(inputStream, new File(toFilePath));
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
        }
    }
}
