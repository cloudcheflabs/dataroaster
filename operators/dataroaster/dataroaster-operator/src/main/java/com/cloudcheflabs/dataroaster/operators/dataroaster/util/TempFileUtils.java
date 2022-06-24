package com.cloudcheflabs.dataroaster.operators.dataroaster.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.UUID;

public class TempFileUtils {

    public static String createTempDirectory() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        String formattedDate = fmt.print(DateTime.now());
        String uid = formattedDate + "-" + UUID.randomUUID().toString();

        // base temp directory.
        String sparkAppBaseTempDir = System.getProperty("java.io.tmpdir") + "/dataroaster";

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
