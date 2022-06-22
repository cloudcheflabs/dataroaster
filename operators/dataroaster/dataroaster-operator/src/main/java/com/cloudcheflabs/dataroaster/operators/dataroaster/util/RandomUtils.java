package com.cloudcheflabs.dataroaster.operators.dataroaster.util;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Random;

public class RandomUtils {

    public static String randomText() {
        byte[] array = new byte[10];
        new Random().nextBytes(array);
        return new String(array);
    }

    public static String randomPassword() {
        String randomText = randomText();
        String bcrypted = BCryptUtils.encodeWithBCrypt(randomText);
        String encodedString = Base64.getEncoder().encodeToString(bcrypted.getBytes());

        return encodedString;
    }
}
