package com.cloudcheflabs.dataroaster.trino.controller.util;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class YamlUtils {

    public static String jsonToYaml(String json) {
        try {
            JsonNode jsonNodeTree = new ObjectMapper().readTree(json);
            return new YAMLMapper().writeValueAsString(jsonNodeTree);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String objectToYaml(Object obj) {
        try {
            JsonNode jsonNodeTree = new ObjectMapper().readTree(JsonUtils.toJson(new ObjectMapper(), obj));
            return new YAMLMapper().writeValueAsString(jsonNodeTree);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> yamlToMap(String yamlString) {
        InputStream inputStream = new ByteArrayInputStream(yamlString.getBytes());
        Yaml yaml = new Yaml();
        return yaml.load(inputStream);
    }
}
