package com.cloudcheflabs.dataroaster.operators.dataroaster.component;


import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.cloudcheflabs.dataroaster.operators.dataroaster.util.TempFileUtils;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import okhttp3.Response;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DBSchemaCreator {

    public static final String DEFAULT_DATAROASTER_NAMESPACE = "dataroaster-operator";

    private static KubernetesClient kubernetesClient;

    public static void main(String[] args) {
        String host = args[0];
        String user = args[1];
        String password = args[2];
        String sqlPath = args[3];

        System.out.printf("args: [%s]\n", JsonUtils.toJson(Arrays.asList(args)));

        // application context.
        ApplicationContext applicationContext = SpringContextSingleton.getInstance();

        // kubernetes client.
        kubernetesClient = applicationContext.getBean(KubernetesClient.class);

        int MAX_COUNT = 20;
        int count = 0;
        String namespace = getNamespace();
        System.out.printf("namespace: [%s]\n", namespace);
        boolean running = true;
        Pod mysqlPod = null;
        // watch mysql pod if it has the status of RUNNING.
        while (running) {
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
                                    mysqlPod = pod;
                                    running = false;
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

        System.out.printf("ready to run sql script...\n");


        final String podName = mysqlPod.getMetadata().getName();
        String[] pathTokens = sqlPath.split("/");
        String fileName = pathTokens[pathTokens.length - 1];

        String targetFileName = "/tmp/" + fileName;

        // file upload to mysql pod.
        System.out.printf("file [" + sqlPath + "] uploading to mysql pod....\n");
        kubernetesClient.pods().inNamespace(namespace)
                .withName(podName)
                .file(targetFileName)
                .upload(Paths.get(sqlPath));

        StringBuffer cmd = new StringBuffer();
        cmd.append("mysql").append(" -u ").append(user).append(" -p").append(password).append(" < ").append(targetFileName);

        String tempDirectory = TempFileUtils.createTempDirectory();

        // make run script.
        String runShell = "run-script.sh";
        String runShellPath = tempDirectory + "/" + runShell;
        // create run helm shell.
        com.cloudcheflabs.dataroaster.common.util.FileUtils.stringToFile(cmd.toString(), runShellPath, true);
        System.out.printf("run-script.sh: %s\n", com.cloudcheflabs.dataroaster.common.util.FileUtils.fileToString(runShellPath, false));

        // upload run script.
        String scriptTargetFile = "/tmp/run-script.sh";
        kubernetesClient.pods().inNamespace(namespace)
                .withName(podName)
                .file(scriptTargetFile)
                .upload(Paths.get(runShellPath));

        String cmdOutput = execCommandOnPod(podName, namespace, scriptTargetFile);
        System.out.println(cmdOutput);

        TempFileUtils.deleteDirectory(tempDirectory);
    }

    public static String execCommandOnPod(String podName, String namespace, String... cmd) {
        Pod pod = kubernetesClient.pods().inNamespace(namespace).withName(podName).get();
        System.out.printf("Running command: [%s] on pod [%s] in namespace [%s]%n",
                Arrays.toString(cmd), pod.getMetadata().getName(), namespace);

        CompletableFuture<String> data = new CompletableFuture<>();
        try (ExecWatch execWatch = execCmd(pod, data, cmd)) {
            return data.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }


    private static ExecWatch execCmd(Pod pod, CompletableFuture<String> data, String... command) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return kubernetesClient.pods()
                .inNamespace(pod.getMetadata().getNamespace())
                .withName(pod.getMetadata().getName())
                .writingOutput(baos)
                .writingError(baos)
                .usingListener(new SimpleListener(data, baos))
                .exec(command);
    }

    static class SimpleListener implements ExecListener {

        private CompletableFuture<String> data;
        private ByteArrayOutputStream baos;

        public SimpleListener(CompletableFuture<String> data, ByteArrayOutputStream baos) {
            this.data = data;
            this.baos = baos;
        }


        @Override
        public void onOpen(Response response) {
            System.out.println("Reading data... ");
        }

        @Override
        public void onFailure(Throwable t, Response failureResponse) {
            System.err.println(t.getMessage());
            data.completeExceptionally(t);
        }

        @Override
        public void onClose(int code, String reason) {
            System.out.println("Exit with: " + code + " and with reason: " + reason);
            data.complete(baos.toString());
        }
    }

    private static String getNamespace() {
        try {
            String namespaceFile = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";
            return FileUtils.fileToString(namespaceFile, false);
        } catch (Exception e) {
            System.out.printf("instead return default dataroaster operator namespace [%s]\n", DEFAULT_DATAROASTER_NAMESPACE);
            return DEFAULT_DATAROASTER_NAMESPACE;
        }
    }
}
