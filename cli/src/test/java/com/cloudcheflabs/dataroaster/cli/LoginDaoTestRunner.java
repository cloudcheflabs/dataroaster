package com.cloudcheflabs.dataroaster.cli;

import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.cli.api.dao.LoginDao;
import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class LoginDaoTestRunner {

    @Test
    public void login() throws Exception {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        String server = "http://localhost:8082";

        LoginDao loginDao = applicationContext.getBean(LoginDao.class);
        RestResponse restResponse = loginDao.login("dataroaster", "dataroaster123", server);

        String json = JsonUtils.toJson(new ObjectMapper(), restResponse);
        System.out.printf("json: \n%s\n", JsonWriter.formatJson(json));


        String authJson = restResponse.getSuccessMessage();
        Map<String, Object> map = JsonUtils.toMap(new ObjectMapper(), authJson);
        String accessToken = (String) map.get("access_token");


        // write json to file.
        ConfigProps configProps = new ConfigProps(server, accessToken);
        try {
            File configFile = DataRoasterConfig.getConfigFile();

            FileWriter file = new FileWriter(configFile);

            // write formatted json to config file.
            file.write(JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), configProps)));
            file.close();

            // read json from file.
            configProps = DataRoasterConfig.getConfigProps();
            System.out.printf("server from file: %s\n", configProps.getServer());
            System.out.printf("accessToken from file: %s\n", configProps.getAccessToken());

        } catch (IOException e) {
            e.printStackTrace();
        }




    }
}
