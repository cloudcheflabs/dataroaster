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
import java.util.Arrays;
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
}