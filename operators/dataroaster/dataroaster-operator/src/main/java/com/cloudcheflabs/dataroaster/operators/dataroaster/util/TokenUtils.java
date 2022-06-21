package com.cloudcheflabs.dataroaster.operators.dataroaster.util;

import java.util.Base64;

public class TokenUtils {

    /**
     * this token will be created for client to send requests with authorization header.
     *
     * @return
     */
    public static String newToken() {
        String generatedString = RandomUtils.randomText();
        String bcrypted = BCryptUtils.encodeWithBCrypt(generatedString);
        String encodedString = Base64.getEncoder().encodeToString(bcrypted.getBytes());

        return encodedString;
    }
}
