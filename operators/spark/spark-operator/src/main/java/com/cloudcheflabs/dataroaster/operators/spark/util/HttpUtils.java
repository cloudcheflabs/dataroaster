package com.cloudcheflabs.dataroaster.operators.spark.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class HttpUtils {

    private static Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

    public static void downloadFileFromHttpRest(String urlPath, String toFilePath) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(urlPath).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(toFilePath)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
        }
    }
}
