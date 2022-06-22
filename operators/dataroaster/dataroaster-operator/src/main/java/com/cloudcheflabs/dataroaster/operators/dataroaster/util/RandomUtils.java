package com.cloudcheflabs.dataroaster.operators.dataroaster.util;

import java.nio.charset.Charset;
import java.util.Random;
import java.util.UUID;

public class RandomUtils {

    public static String randomText() {
        byte[] array = new byte[20];
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
    }

    public static String randomPassword() {
        UUID randomUUID = UUID.randomUUID();

        return randomUUID.toString().replaceAll("-", "");
    }
}
