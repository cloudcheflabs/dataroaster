package com.cloudcheflabs.dataroaster.trino.gateway.util;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class DeflateUtils {
    public static byte[] deflateCompressBytes(final byte[] input) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final byte[] buf = new byte[1024];
        final Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION);
        compresser.setInput(input);
        compresser.finish();
        while (!compresser.finished()) {
            final int compressedDataLength = compresser.deflate(buf);
            bos.write(buf, 0, compressedDataLength);
        }
        compresser.end();
        return bos.toByteArray();
    }

    public static byte[] deflateDecompressBytes(final byte[] input) throws DataFormatException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final byte[] buf = new byte[1024];
        final Inflater decompresser = new Inflater();
        decompresser.setInput(input, 0, input.length);
        while (!decompresser.finished()) {
            final int resultLength = decompresser.inflate(buf);
            bos.write(buf, 0, resultLength);
        }
        decompresser.end();
        return bos.toByteArray();
    }
}
