package com.cloudcheflabs.dataroaster.operators.dataroaster.controller;

import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.CustomResource;
import com.cloudcheflabs.dataroaster.operators.dataroaster.util.IdUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        String namespace = (String) metadataMap.get("namespace");

        String newId = IdUtils.newId(Arrays.asList(kind, name, namespace));

        CustomResource customResource = new CustomResource();
        customResource.setId(newId);
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


    public static List<String> getSparkApplicationPvcNames(String yamlContents) {

        InputStream inputStream = new ByteArrayInputStream(yamlContents.getBytes());
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(inputStream);
        LOG.debug(JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), map)));

        String kind = (String) map.get("kind");
        Map<String, Object> specMap = (Map<String, Object>) map.get("spec");
        if(kind.equals("SparkApplication")) {
            List<String> pvcList = new ArrayList<>();
            List<Map<String, Object>> volumesList = (List<Map<String, Object>>) specMap.get("volumes");
            for(Map<String, Object> volumeMap : volumesList) {
                Map<String, Object> pvcMap = (Map<String, Object>) volumeMap.get("persistentVolumeClaim");
                String pvc = (String) pvcMap.get("claimName");
                pvcList.add(pvc);
            }

            return pvcList;
        } else {
            throw new RuntimeException("Not Supported Custom Resource: \n" + yamlContents);
        }
    }
}