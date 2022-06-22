package com.cloudcheflabs.dataroaster.operators.dataroaster.util;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Random;

public class RandomUtils {

    public static String randomText() {
        byte[] array = new byte[20];
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
    }

    public static String randomPassword() {
        String randomText = randomText();
        String encodedString = Base64.getEncoder().encodeToString(randomText);

        return encodedString;
    }
}
