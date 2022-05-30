package com.cloudcheflabs.dataroaster.cli.config;

import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class DataRoasterConfig {

    public static File getConfigFile() {
        String dataroasterHome = System.getProperty("user.home") + "/.dataroaster";
        File f = new File(dataroasterHome);
        if(!f.exists()) {
            f.mkdir();
        }
        String configFile = dataroasterHome + "/config";

        return new File(configFile);
    }

    public static ConfigProps getConfigProps() {
        try {
            return new ObjectMapper().readValue(getConfigFile(), ConfigProps.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
