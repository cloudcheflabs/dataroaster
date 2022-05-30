package com.cloudcheflabs.dataroaster.common.util;

import java.util.Base64;

public class StringUtils {

    public static String base64Encode(String string) {
        return new String(Base64.getEncoder().encode(string.getBytes()));
    }

    public static String base64Decode(String string) {
        return new String(Base64.getDecoder().decode(string.getBytes()));
    }
}
