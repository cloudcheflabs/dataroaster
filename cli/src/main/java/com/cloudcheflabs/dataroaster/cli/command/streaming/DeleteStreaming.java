package com.cloudcheflabs.dataroaster.cli.command.streaming;

import com.cloudcheflabs.dataroaster.cli.api.dao.ServicesDao;
import com.cloudcheflabs.dataroaster.cli.command.CommandUtils;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.cli.domain.ServiceDef;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "delete",
        subcommands = { CommandLine.HelpCommand.class },
        description = "Delete Streaming.")
public class DeleteStreaming implements Callable<Integer> {

    @CommandLine.ParentCommand
    private Streaming parent;

    @Override
    public Integer call() throws Exception {
        ConfigProps configProps = parent.configProps;

        java.io.Console cnsl = System.console();
        if (cnsl == null) {
            System.out.println("No console available");
            return -1;
        }

        // show services list.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        ServicesDao servicesDao = applicationContext.getBean(ServicesDao.class);
        RestResponse restResponse = servicesDao.listServices(configProps);

        // if response status code is not ok, then throw an exception.
        if(restResponse.getStatusCode() != RestResponse.STATUS_OK) {
            throw new RuntimeException(restResponse.getErrorMessage());
        }

        List<Map<String, Object>> servicesList =
                JsonUtils.toMapList(new ObjectMapper(), restResponse.getSuccessMessage());

        String format = "%-20s%-20s%-20s%-20s%n";

        System.out.printf(format,"SERVICE ID", "SERVICE TYPE", "CLUSTER NAME", "PROJECT NAME");
        for(Map<String, Object> map : servicesList) {
            String serviceType = (String) map.get("serviceDefType");
            if(serviceType.equals(ServiceDef.ServiceTypeEnum.STREAMING.name())) {
                System.out.printf(format,
                        String.valueOf(map.get("id")),
                        (String) map.get("serviceDefType"),
                        (String) map.get("clusterName"),
                        (String) map.get("projectName"));
            }
        }

        System.out.printf("\n");

        String serviceId = cnsl.readLine("Select Service ID to be deleted : ");
        while(serviceId.equals("")) {
            System.err.println("service id is required!");
            serviceId = cnsl.readLine("Select Service ID to be deleted : ");
            if(!serviceId.equals("")) {
                break;
            }
        }

        System.out.printf("\n");

        // delete.
        return CommandUtils.deleteStreaming(configProps, serviceId);
    }
}
