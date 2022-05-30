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

public class QueryEngineHandler {

    private static Logger LOG = LoggerFactory.getLogger(QueryEngineHandler.class);

    private static String moveFiles(Kubeconfig kubeconfig) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        String formattedDate = fmt.print(DateTime.now());
        String uid = formattedDate + "-" + UUID.randomUUID().toString();
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/query-engine/" + uid;

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        String rootPath = "/templates/query-engine";
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath, "1.0.0", tempDirectory);
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0", "spark-thrift-server", tempDirectory + "/spark-thrift-server");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0/spark-thrift-server", "templates", tempDirectory + "/spark-thrift-server/templates");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0", "trino", tempDirectory + "/trino");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0/trino", "templates", tempDirectory + "/trino/templates");
       
        // kubeconfig raw yaml.
        String kubeconfigName = "kubeconfig";
        String kubeconfigString = kubeconfig.getRawKubeconfig();

        // write kubeconfig file.
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/spark-thrift-server/" + kubeconfigName, false);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/trino/" + kubeconfigName, false);
      
        return tempDirectory;
    }

    public static String create(Kubeconfig kubeconfig, Map<String, ?> map) {
        try {
            runProcess(kubeconfig, map, "create.sh");
            return "query engine created...";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static String delete(Kubeconfig kubeconfig) {
        try {
            Map<String, Object> map = new HashMap<>();

            runProcess(kubeconfig, map, "delete.sh");
            return "query engine deleted...";
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

        // spark-thrift-server.
        files = Arrays.asList(
                "create.sh",
                "delete.sh",
                "spark-thrift-server-service.yaml"
        );

        for (String file : files) {
            Map<String, Object> kv = new HashMap<>();
            kv.put("tempDirectory", tempDirectory);
            kv.put("kubeconfig", "kubeconfig");
            kv.put("sparkThriftServerNamespace", K8sNamespace.DEFAULT_NAMESPACE_SPARK_THRIFT_SERVER);
            kv.put("sparkThriftServerStorageClass", (String) map.get("sparkThriftServerStorageClass"));
            kv.put("k8sServer", kubeconfig.getMasterUrl());
            kv.put("s3Bucket", (String) map.get("s3Bucket"));
            kv.put("s3AccessKey", (String) map.get("s3AccessKey"));
            kv.put("s3SecretKey", (String) map.get("s3SecretKey"));
            kv.put("s3Endpoint", (String) map.get("s3Endpoint"));
            kv.put("hivemetastoreNamespace", K8sNamespace.DEFAULT_NAMESPACE_HIVE_METASTORE);
            kv.put("sparkThriftServerExecutors", (Integer) map.get("sparkThriftServerExecutors"));
            kv.put("sparkThriftServerExecutorMemory", (Integer) map.get("sparkThriftServerExecutorMemory"));
            kv.put("sparkThriftServerExecutorCores", (Integer) map.get("sparkThriftServerExecutorCores"));
            kv.put("sparkThriftServerDriverMemory", (Integer) map.get("sparkThriftServerDriverMemory"));

            TemplateUtils.toFile(tempDirectory + "/spark-thrift-server/" + file,
                    false,
                    kv,
                    tempDirectory + "/spark-thrift-server/" + file,
                    true);
        }

        // trino.
        files = Arrays.asList(
                "create.sh",
                "delete.sh",
                "dataroaster-values.yaml"
        );

        for (String file : files) {
            Map<String, Object> kv = new HashMap<>();
            kv.put("kubeconfig", "kubeconfig");
            kv.put("trinoNamespace", K8sNamespace.DEFAULT_NAMESPACE_TRINO);
            kv.put("trinoWorkers", (Integer) map.get("trinoWorkers"));
            kv.put("trinoServerMaxMemory", (Integer) map.get("trinoServerMaxMemory"));
            kv.put("trinoCores", (Integer) map.get("trinoCores"));
            kv.put("trinoTempDataStorage", (Integer) map.get("trinoTempDataStorage"));
            kv.put("trinoDataStorage", (Integer) map.get("trinoDataStorage"));
            kv.put("trinoStorageClass", (String) map.get("trinoStorageClass"));
            kv.put("sparkThriftServerStorageClass", (String) map.get("sparkThriftServerStorageClass"));
            kv.put("s3AccessKey", (String) map.get("s3AccessKey"));
            kv.put("s3SecretKey", (String) map.get("s3SecretKey"));
            kv.put("s3Endpoint", (String) map.get("s3Endpoint"));
            kv.put("hivemetastoreNamespace", K8sNamespace.DEFAULT_NAMESPACE_HIVE_METASTORE);

            TemplateUtils.toFile(tempDirectory + "/trino/" + file,
                    false,
                    kv,
                    tempDirectory + "/trino/" + file,
                    true);
        }

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + runShell);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);
    }
}
