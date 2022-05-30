package com.cloudcheflabs.dataroaster.apiserver.kubernetes.handler;

import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.model.K8sNamespace;
import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.common.util.ProcessExecutor;
import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PrivateRegistryHandler {

    private static Logger LOG = LoggerFactory.getLogger(PrivateRegistryHandler.class);

    private static String moveFiles(Kubeconfig kubeconfig) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        String formattedDate = fmt.print(DateTime.now());
        String uid = formattedDate + "-" + UUID.randomUUID().toString();
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/private-registry/" + uid;

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        String rootPath = "/templates/private-registry";
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath, "1.0.0", tempDirectory);

        // kubeconfig raw yaml.
        String kubeconfigName = "kubeconfig";
        String kubeconfigString = kubeconfig.getRawKubeconfig();

        // write kubeconfig file.
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/" + kubeconfigName, false);

        return tempDirectory;
    }

    public static String create(Kubeconfig kubeconfig, Map<String, ?> map) {
        try {
            runProcess(kubeconfig, map, "create.sh");
            return "private registry created...";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static String delete(Kubeconfig kubeconfig) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("coreHost", "");
            map.put("notaryHost", "");
            map.put("storageClass", "");
            map.put("registryStorageSize", "");
            map.put("chartmuseumStorageSize", "");
            map.put("jobserviceStorageSize", "");
            map.put("databaseStorageSize", "");
            map.put("redisStorageSize", "");
            map.put("trivyStorageSize", "");
            map.put("s3Bucket", "");
            map.put("s3AccessKey", "");
            map.put("s3SecretKey", "");
            map.put("s3Endpoint", "");

            runProcess(kubeconfig, map, "delete.sh");
            return "private registry deleted...";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static void runProcess(Kubeconfig kubeconfig, Map<String, ?> map, String runShell) {
        String tempDirectory = moveFiles(kubeconfig);

        // root dir files.
        List<String> files = Arrays.asList(
                "create.sh",
                "delete.sh",
                "dataroaster-harbor.yaml"
        );
        for (String file : files) {
            Map<String, String> kv = new HashMap<>();
            kv.put("tempDirectory", tempDirectory);
            kv.put("kubeconfig", "kubeconfig");
            kv.put("namespace", K8sNamespace.DEFAULT_NAMESPACE_HARBOR);
            kv.put("coreHost", (String) map.get("coreHost"));
            kv.put("notaryHost", (String) map.get("notaryHost"));
            kv.put("storageClass", (String) map.get("storageClass"));
            kv.put("registryStorageSize", (String) map.get("registryStorageSize"));
            kv.put("chartmuseumStorageSize", (String) map.get("chartmuseumStorageSize"));
            kv.put("jobserviceStorageSize", (String) map.get("jobserviceStorageSize"));
            kv.put("databaseStorageSize", (String) map.get("databaseStorageSize"));
            kv.put("redisStorageSize", (String) map.get("redisStorageSize"));
            kv.put("trivyStorageSize", (String) map.get("trivyStorageSize"));
            kv.put("s3Bucket", (String) map.get("s3Bucket"));
            kv.put("s3AccessKey", (String) map.get("s3AccessKey"));
            kv.put("s3SecretKey", (String) map.get("s3SecretKey"));
            kv.put("s3Endpoint", (String) map.get("s3Endpoint"));

            TemplateUtils.toFile(tempDirectory + "/" + file,
                    false,
                    kv,
                    tempDirectory + "/" + file,
                    true);
        }

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + runShell);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);
    }
}
