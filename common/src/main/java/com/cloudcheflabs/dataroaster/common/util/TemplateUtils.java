package com.cloudcheflabs.dataroaster.common.util;

import com.hubspot.jinjava.Jinjava;

import java.util.Map;

public class TemplateUtils {

    public static String replace(String templatePath, boolean fromClasspath, Map<String, ?> kv) {
        String templateString = FileUtils.fileToString(templatePath, fromClasspath);
        return replace(templateString, kv);
    }

    public static String replace(String templateString, Map<String, ?> kv) {
        Jinjava jinjava = new Jinjava();
        return jinjava.render(templateString, kv);
    }

    public static void toFile(String templatePath, boolean fromClasspath, Map<String, ?> kv, String targetFilePath, boolean executable) {
        FileUtils.stringToFile(replace(templatePath, fromClasspath, kv), targetFilePath, executable);
    }
}
