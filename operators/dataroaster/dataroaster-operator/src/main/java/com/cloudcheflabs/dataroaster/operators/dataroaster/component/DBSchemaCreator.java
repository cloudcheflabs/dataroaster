package com.cloudcheflabs.dataroaster.operators.dataroaster.component;


import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.operators.dataroaster.util.TempFileUtils;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DBSchemaCreator {

    private static Logger LOG = LoggerFactory.getLogger(DBSchemaCreator.class);

    public static final String DEFAULT_DATAROASTER_NAMESPACE = "dataroaster-operator";

    public static void main(String[] args) {
        String host = args[0];
        String user = args[1];
        String password = args[2];
        String sqlPath = args[3];

        System.out.println("=========");

        LOG.info("args: [{}]", JsonUtils.toJson(Arrays.asList(args)));

        // application context.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // kubernetes client.
        KubernetesClient kubernetesClient = applicationContext.getBean(KubernetesClient.class);

        int MAX_COUNT = 20;
        int count = 0;
        // watch mysql pod if it has the status of RUNNING.
        while (true) {
            PodList podList = kubernetesClient.pods().inNamespace(getNamespace()).list();
            for(Pod pod : podList.getItems()) {
                ObjectMeta metadata = pod.getMetadata();
                Map<String, String> labels = metadata.getLabels();
                for(String key : labels.keySet()) {
                    if(key.equals("app")) {
                        String value = labels.get(key);
                        if(value.equals("mysql")) {
                            PodStatus status = pod.getStatus();
                            List<ContainerStatus> containerStatuses = status.getContainerStatuses();
                            if (!containerStatuses.isEmpty()) {
                                ContainerStatus containerStatus = containerStatuses.get(0);
                                ContainerState state = containerStatus.getState();
                                ContainerStateRunning containerStateRunning = state.getRunning();
                                if(containerStateRunning != null) {
                                    LOG.info("mysql has running status now.");
                                    runSqlScript(host, user, password, sqlPath);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if(count < MAX_COUNT) {
                count++;
                try {
                    LOG.info("mysql is not running status now...");
                    Thread.sleep(5000);
                    continue;
                } catch (Exception e) {
                    LOG.error("error", e);
                }
            } else {
                throw new IllegalStateException("mysql has no running status!");
            }
        }
    }

    private static void runSqlScript(String host, String user, String password, String sqlPath) {
        // run command to create db schema.
        String tempDirectory = TempFileUtils.createTempDirectory();
        String runShell = "run-mysql-query.sh";
        String runShellPath = tempDirectory + "/" + runShell;

        // mysql -h localhost -u root -pmysqlpass123 < /opt/dataroaster-operator/create-tables.sql
        StringBuffer cmd = new StringBuffer();
        cmd.append("mysql -h ").append(host).append(" -u ").append(user).append(" -p").append(password).append(" < ").append(sqlPath);

        // create run helm shell.
        FileUtils.stringToFile(cmd.toString(), runShellPath, true);
        LOG.info("run-mysql-query.sh: \n{}", FileUtils.fileToString(runShellPath, false));

        // run helm shell.
        DBSchemaProcessExecutor processExecutor = new DBSchemaProcessExecutor();
        processExecutor.doExec(runShellPath);

        TempFileUtils.deleteDirectory(tempDirectory);
    }

    public static String getNamespace() {
        try {
            String namespaceFile = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";
            return FileUtils.fileToString(namespaceFile, false);
        } catch (Exception e) {
            LOG.warn("instead return default dataroaster operator namespace [{}]", DEFAULT_DATAROASTER_NAMESPACE);
            return DEFAULT_DATAROASTER_NAMESPACE;
        }
    }
}
