package com.cloudcheflabs.dataroaster.cli.command.login;

import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.cli.api.dao.LoginDao;
import com.cloudcheflabs.dataroaster.cli.command.Console;
import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "login",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Login to DataRoaster API Server.")
public class Login implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Console parent;

    @CommandLine.Parameters(index = "0", description = "DataRoaster API Server URL", defaultValue = "http://localhost:8082")
    private String server;

    @Override
    public Integer call() throws Exception {
        java.io.Console cnsl = System.console();

        if (cnsl == null) {
            System.out.println("No console available");
            return -1;
        }
        String user = cnsl.readLine("Enter username : ");
        char[] password = cnsl.readPassword("Enter password : ");

        // spring application context.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // login.
        LoginDao loginDao = applicationContext.getBean(LoginDao.class);
        RestResponse restResponse = loginDao.login(user, new String(password), server);

        if(restResponse.getStatusCode() != 200) {
            System.err.println(restResponse.getErrorMessage());
            return -1;
        }

        // save server url and access token in config file.
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

            System.out.println("login success!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // null out the arrays when done.
        Arrays.fill(password, ' ');

        return 0;
    }
}
