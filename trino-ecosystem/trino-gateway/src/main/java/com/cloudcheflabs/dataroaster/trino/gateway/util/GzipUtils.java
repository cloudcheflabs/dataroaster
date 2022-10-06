package com.cloudcheflabs.dataroaster.trino.gateway.util;

import com.cloudcheflabs.dataroaster.trino.gateway.proxy.TrinoProxyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
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


    public static String plainTextFromGz(byte[] compressed) {
        final StringBuilder outStr = new StringBuilder();
        if ((compressed == null) || (compressed.length == 0)) {
            return "";
        }
        if (isGzipCompressed(compressed)) {
            GZIPInputStream gis = null;
            try {
                gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
                final BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(gis, Charset.defaultCharset()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    outStr.append(line);
                }
                gis.close();
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("exception: {}", e);
                try {
                    gis.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        } else {
            outStr.append(compressed);
        }
        return outStr.toString();
    }

    public static boolean isGzipCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
                && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
}
