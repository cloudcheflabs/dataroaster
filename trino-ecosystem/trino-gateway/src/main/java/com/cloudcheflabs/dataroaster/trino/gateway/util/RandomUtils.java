package com.cloudcheflabs.dataroaster.trino.gateway.util;

import java.util.List;
import java.util.Random;

public class RandomUtils {

    public static <T> T randomize(List<T> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }
}
