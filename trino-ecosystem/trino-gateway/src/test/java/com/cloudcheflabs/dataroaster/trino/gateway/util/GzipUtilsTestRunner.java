package com.cloudcheflabs.dataroaster.trino.gateway.util;

import org.junit.Test;

public class GzipUtilsTestRunner {

    @Test
    public void compressGzip() throws Exception {
        String json = "{\"id\": 2, \"count\": 4}";

        byte[] gzippedBytes = GzipUtils.compressStringInGzip(json);

        String decompressedJson = GzipUtils.decompressGzip(gzippedBytes);
        System.out.println(decompressedJson);
    }



    @Test
    public void decompressGzip() throws Exception {
        String json = "{\"id\": 2, \"count\": 4}";

        byte[] gzippedBytes = GzipUtils.compressStringInGzip(json);

        String decompressedJson = GzipUtils.decompressGzip(gzippedBytes);
        System.out.println(decompressedJson);
    }
}
