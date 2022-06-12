package com.cloudcheflabs.dataroaster.trino.gateway.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class BCryptUtils {
    private static final int DEFAULT_COST = 8;

    public static String encodeWithBCrypt(String str) {
        return BCrypt.withDefaults().hashToString(DEFAULT_COST, str.toCharArray());
    }

    public static boolean isMatched(String str, String bcryptEncodedStr) {
        BCrypt.Result result = BCrypt.verifyer().verify(str.getBytes(), bcryptEncodedStr.getBytes());
        return result.verified;
    }
}
