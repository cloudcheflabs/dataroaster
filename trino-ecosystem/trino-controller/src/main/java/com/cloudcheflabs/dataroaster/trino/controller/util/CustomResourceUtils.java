package com.cloudcheflabs.dataroaster.trino.controller.util;

import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.trino.controller.domain.CustomResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

public class CustomResourceUtils {

    private static Logger LOG = LoggerFactory.getLogger(CustomResourceUtils.class);

    public static CustomResource fromYaml(String yamlContents) {

        InputStream inputStream = new ByteArrayInputStream(yamlContents.getBytes());
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(inputStream);
        LOG.debug(JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), map)));

        String kind = (String) map.get("kind");

        Map<String, Object> metadataMap = (Map<String, Object>) map.get("metadata");
        String name = (String) metadataMap.get("name");
        String namespace = metadataMap.containsKey("namespace") ? (String) metadataMap.get("namespace") : null;

        CustomResource customResource = new CustomResource();
        customResource.setKind(kind);
        customResource.setName(name);
        customResource.setNamespace(namespace);
        customResource.setYaml(yamlContents);

        LOG.info("custom resource: \n{}", JsonUtils.toJson(customResource));

        return customResource;
    }


    public static String getTargetNamespace(String yamlContents) {

        InputStream inputStream = new ByteArrayInputStream(yamlContents.getBytes());
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(inputStream);
        LOG.debug(JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), map)));

        String kind = (String) map.get("kind");
        Map<String, Object> specMap = (Map<String, Object>) map.get("spec");
        if(kind.equals("HelmChart") || kind.equals("TrinoCluster")) {
            return (String) specMap.get("namespace");
        } else if(kind.equals("SparkApplication")) {
            Map<String, Object> coreMap = (Map<String, Object>) specMap.get("core");
            return (String) coreMap.get("namespace");
        } else {
            throw new RuntimeException("Not Supported Custom Resource: \n" + yamlContents);
        }
    }
}