package com.cloudcheflabs.dataroaster.trino.controller.util;

public class PauseUtils {

    public static void pause(long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
