package com.cloudcheflabs.dataroaster.operators.helm.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.UUID;

public class FileUtils {

    public static String createHelmTempDirectory() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        String formattedDate = fmt.print(DateTime.now());
        String uid = formattedDate + "-" + UUID.randomUUID().toString();

        // base temp directory.
        String sparkAppBaseTempDir = System.getProperty("java.io.tmpdir") + "/helm";

        // temp directory.
        String tempDirectory = sparkAppBaseTempDir + "/" + uid;

        // create temp. directory.
        com.cloudcheflabs.dataroaster.common.util.FileUtils.createDirectory(tempDirectory);

        return tempDirectory;
    }

    public static void deleteDirectory(String path) {
        com.cloudcheflabs.dataroaster.common.util.FileUtils.deleteDirectory(path);
    }
}
