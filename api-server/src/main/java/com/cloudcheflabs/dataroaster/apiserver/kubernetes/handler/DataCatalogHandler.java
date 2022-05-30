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

public class DataCatalogHandler {

    private static Logger LOG = LoggerFactory.getLogger(DataCatalogHandler.class);

    private static String moveFiles(Kubeconfig kubeconfig) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        String formattedDate = fmt.print(DateTime.now());
        String uid = formattedDate + "-" + UUID.randomUUID().toString();
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/data-catalog/" + uid;

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        String rootPath = "/templates/data-catalog";
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath, "1.0.0", tempDirectory);
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0", "init-schema", tempDirectory + "/init-schema");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0/init-schema", "templates", tempDirectory + "/init-schema/templates");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0", "metastore", tempDirectory + "/metastore");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0/metastore", "templates", tempDirectory + "/metastore/templates");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0", "mysql", tempDirectory + "/mysql");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0/mysql", "templates", tempDirectory + "/mysql/templates");

        // kubeconfig raw yaml.
        String kubeconfigName = "kubeconfig";
        String kubeconfigString = kubeconfig.getRawKubeconfig();

        // write kubeconfig file.
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/init-schema/" + kubeconfigName, false);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/metastore/" + kubeconfigName, false);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/mysql/" + kubeconfigName, false);

        return tempDirectory;
    }

    public static String create(Kubeconfig kubeconfig, Map<String, ?> map) {
        try {
            runProcess(kubeconfig, map, "create.sh");
            return "data catalog created...";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static String delete(Kubeconfig kubeconfig) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("s3Bucket", "");
            map.put("s3AccessKey", "");
            map.put("s3SecretKey", "");
            map.put("s3Endpoint", "");
            map.put("storageClass", "");
            map.put("storageSize", -1);

            runProcess(kubeconfig, map, "delete.sh");
            return "data catalog deleted...";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static void runProcess(Kubeconfig kubeconfig, Map<String, ?> map, String runShell) {
        String tempDirectory = moveFiles(kubeconfig);

        // root dir files.
        List<String> files = Arrays.asList(
                "create.sh",
                "delete.sh"
        );
        for (String file : files) {
            Map<String, Object> kv = new HashMap<>();
            kv.put("tempDirectory", tempDirectory);

            TemplateUtils.toFile(tempDirectory + "/" + file,
                    false,
                    kv,
                    tempDirectory + "/" + file,
                    true);
        }

        // init-schema.
        files = Arrays.asList(
                "create.sh",
                "delete.sh",
                "dataroaster-values.yaml"
        );
        for (String file : files) {
            Map<String, Object> kv = new HashMap<>();
            kv.put("kubeconfig", "kubeconfig");
            kv.put("namespace", K8sNamespace.DEFAULT_NAMESPACE_HIVE_METASTORE);

            TemplateUtils.toFile(tempDirectory + "/init-schema/" + file,
                    false,
                    kv,
                    tempDirectory + "/init-schema/" + file,
                    true);
        }

        // metastore.
        files = Arrays.asList(
                "create.sh",
                "delete.sh",
                "dataroaster-values.yaml"
        );

        for (String file : files) {
            Map<String, Object> kv = new HashMap<>();
            kv.put("kubeconfig", "kubeconfig");
            kv.put("namespace", K8sNamespace.DEFAULT_NAMESPACE_HIVE_METASTORE);
            kv.put("s3Bucket", (String) map.get("s3Bucket"));
            kv.put("s3AccessKey", (String) map.get("s3AccessKey"));
            kv.put("s3SecretKey", (String) map.get("s3SecretKey"));
            kv.put("s3Endpoint", (String) map.get("s3Endpoint"));

            TemplateUtils.toFile(tempDirectory + "/metastore/" + file,
                    false,
                    kv,
                    tempDirectory + "/metastore/" + file,
                    true);
        }

        // mysql.
        files = Arrays.asList(
                "create.sh",
                "delete.sh",
                "dataroaster-values.yaml"
        );

        for (String file : files) {
            Map<String, Object> kv = new HashMap<>();
            kv.put("kubeconfig", "kubeconfig");
            kv.put("namespace", K8sNamespace.DEFAULT_NAMESPACE_HIVE_METASTORE);
            kv.put("storageClass", (String) map.get("storageClass"));
            kv.put("storageSize", (Integer) map.get("storageSize"));

            TemplateUtils.toFile(tempDirectory + "/mysql/" + file,
                    false,
                    kv,
                    tempDirectory + "/mysql/" + file,
                    true);
        }

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + runShell);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);
    }
}
