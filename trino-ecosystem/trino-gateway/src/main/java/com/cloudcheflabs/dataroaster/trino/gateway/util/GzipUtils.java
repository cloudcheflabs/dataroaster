package com.cloudcheflabs.dataroaster.trino.gateway.util;

import okio.Buffer;
import okio.GzipSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {

    private static Logger LOG = LoggerFactory.getLogger(GzipUtils.class);

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
        Buffer gzippedBuffer = new Buffer().write(compressedData);

        Buffer result = new Buffer();
        GzipSource source = new GzipSource(gzippedBuffer);
        try {
            while (source.read(result, Integer.MAX_VALUE) != -1) {
            }
            return result.readUtf8().getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String("").getBytes();
    }

    public static boolean isGzipCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
                && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
}
