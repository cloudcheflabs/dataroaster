package com.cloudcheflabs.dataroaster.trino.gateway.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {

    public static byte[] compressStringInGzip(String content) {
        return compress(content.getBytes());
    }


    private static byte[] compress(byte[] uncompressedData) {
        ByteArrayOutputStream bos = null;
        GZIPOutputStream gzipOS = null;
        try {
            bos = new ByteArrayOutputStream(uncompressedData.length);
            gzipOS = new GZIPOutputStream(bos);
            gzipOS.write(uncompressedData);
            gzipOS.close();
            return bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                assert gzipOS != null;
                gzipOS.close();
                bos.close();
            }
            catch (Exception ignored) {
            }
        }
        return new byte[]{};
    }


    public static String decompressGzip(byte[] bytes) {
        return new String(uncompress(bytes));
    }


    private static byte[] uncompress(byte[] compressedData) {
        ByteArrayInputStream bis = null;
        ByteArrayOutputStream bos = null;
        GZIPInputStream gzipIS = null;

        try {
            bis = new ByteArrayInputStream(compressedData);
            bos = new ByteArrayOutputStream();
            gzipIS = new GZIPInputStream(bis);

            byte[] buffer = new byte[1024];
            int len;
            while((len = gzipIS.read(buffer)) != -1){
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                assert gzipIS != null;
                gzipIS.close();
                bos.close();
                bis.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new byte[]{};
    }
}
