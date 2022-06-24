package com.cloudcheflabs.dataroaster.operators.dataroaster.component;


import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.operators.dataroaster.util.TempFileUtils;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DBSchemaCreator {

    public static final String DEFAULT_DATAROASTER_NAMESPACE = "dataroaster-operator";

    public static void main(String[] args) {
        String host = args[0];
        String user = args[1];
        String password = args[2];
        String sqlPath = args[3];

        System.out.printf("args: [%s]\n", JsonUtils.toJson(Arrays.asList(args)));

        // application context.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // kubernetes client.
        KubernetesClient kubernetesClient = applicationContext.getBean(KubernetesClient.class);

        int MAX_COUNT = 20;
        int count = 0;
        String namespace = getNamespace();
        System.out.printf("namespace: [%s]\n", namespace);
        // watch mysql pod if it has the status of RUNNING.
        while (true) {
            PodList podList = kubernetesClient.pods().inNamespace(namespace).list();
            for(Pod pod : podList.getItems()) {
                ObjectMeta metadata = pod.getMetadata();
                System.out.printf("metadata: [%s]\n", JsonUtils.toJson(metadata));
                Map<String, String> labels = metadata.getLabels();
                System.out.printf("labels: [%s]\n", JsonUtils.toJson(labels));
                for(String key : labels.keySet()) {
                    System.out.printf("key: [%s]\n", key);
                    if(key.equals("app")) {
                        String value = labels.get(key);
                        System.out.printf("key: [%s], value: [%s]\n", key, value);
                        if(value.equals("mysql")) {
                            PodStatus status = pod.getStatus();
                            List<ContainerStatus> containerStatuses = status.getContainerStatuses();
                            System.out.printf("containerStatuses: [%d]\n", containerStatuses.size());
                            if (!containerStatuses.isEmpty()) {
                                ContainerStatus containerStatus = containerStatuses.get(0);
                                ContainerState state = containerStatus.getState();
                                System.out.printf("state: [%s]\n", state.toString());
                                ContainerStateRunning containerStateRunning = state.getRunning();
                                if(containerStateRunning != null) {
                                    System.out.printf("mysql has running status now.\n");
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
                    System.out.printf("mysql is not running status now...\n");
                    Thread.sleep(5000);
                    continue;
                } catch (Exception e) {
                    System.err.println(e);
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
        System.out.printf("run-mysql-query.sh: \n%s\n", FileUtils.fileToString(runShellPath, false));

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
            System.out.printf("instead return default dataroaster operator namespace [%s]\n", DEFAULT_DATAROASTER_NAMESPACE);
            return DEFAULT_DATAROASTER_NAMESPACE;
        }
    }
}
