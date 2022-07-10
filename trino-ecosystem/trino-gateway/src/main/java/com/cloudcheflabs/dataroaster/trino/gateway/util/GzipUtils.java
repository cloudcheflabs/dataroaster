package com.cloudcheflabs.dataroaster.trino.gateway.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {

    public static byte[] compressStringInGzip(String content) {
        byte[] bytes;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(os);

            InputStream is = new ByteArrayInputStream(content.getBytes());
            byte[] buffer = new byte[1024];
            int len;
            while((len = is.read(buffer)) != -1){
                gos.write(buffer, 0, len);
            }

            bytes = os.toByteArray();

            gos.close();
            os.close();
            is.close();

            return bytes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decompressGzip(byte[] bytes) {
        String content;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            InputStream is = new ByteArrayInputStream(bytes);
            GZIPInputStream gis = new GZIPInputStream(is);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            content = new String(os.toByteArray());
            os.close();
            gis.close();
            is.close();
            return content;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
