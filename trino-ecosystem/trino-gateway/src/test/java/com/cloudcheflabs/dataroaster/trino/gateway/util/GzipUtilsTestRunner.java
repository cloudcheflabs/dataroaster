package com.cloudcheflabs.dataroaster.trino.gateway.util;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class GzipUtilsTestRunner {

    @Test
    public void compressGzip() throws Exception {
        String json = "{\"id\": 2, \"count\": 4}";

        byte[] gzippedBytes = GzipUtils.compressStringInGzip(json);
        Assert.assertTrue(GzipUtils.isGzipCompressed(gzippedBytes));

        String decompressedJson = GzipUtils.plainTextFromGz(gzippedBytes);
        System.out.println(decompressedJson);
    }


    @Test
    public void uncompressGzip() throws Exception {
        InputStream is = FileUtils.readFileFromClasspath("/gzip/amendments.txt.gz");
        byte[] gzippedBytes = IOUtils.toByteArray(is);
        Assert.assertTrue(GzipUtils.isGzipCompressed(gzippedBytes));

        String decompressedJson = GzipUtils.plainTextFromGz(gzippedBytes);
        System.out.println(decompressedJson);
    }
}
