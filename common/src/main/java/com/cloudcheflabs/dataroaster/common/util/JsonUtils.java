package com.cloudcheflabs.dataroaster.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    public static Map<String, Object> toMap(ObjectMapper mapper, File file)
    {
        try {
            Map<String, Object> map = mapper.readValue(file, Map.class);
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> toMap(ObjectMapper mapper, String json)
    {
        try {
            Map<String, Object> map = mapper.readValue(json, Map.class);
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, Object>> toMapList(ObjectMapper mapper, String json)
    {
        try {
            List<Map<String, Object>> list = (List<Map<String, Object>>) mapper.readValue(json, List.class);
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJson(ObjectMapper mapper, Object obj)
    {
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
