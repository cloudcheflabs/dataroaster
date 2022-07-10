package com.cloudcheflabs.dataroaster.trino.gateway.util;

import java.util.Base64;

public class Base64Utils {

    public static String encodeBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    public static String decodeBase64(String encoded) {
        byte[] decodedBytes = Base64.getDecoder().decode(encoded);
        return new String(decodedBytes);
    }
}
