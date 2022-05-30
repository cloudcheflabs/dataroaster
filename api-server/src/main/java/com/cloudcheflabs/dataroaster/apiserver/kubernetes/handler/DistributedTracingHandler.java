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

public class DistributedTracingHandler {

    private static Logger LOG = LoggerFactory.getLogger(DistributedTracingHandler.class);

    private static String moveFiles(Kubeconfig kubeconfig) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        String formattedDate = fmt.print(DateTime.now());
        String uid = formattedDate + "-" + UUID.randomUUID().toString();
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/distributed-tracing/" + uid;

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        String rootPath = "/templates/distributed-tracing";
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
            return "distributed tracing created...";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static String delete(Kubeconfig kubeconfig) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("storageClass", "");
            map.put("ingressHost", "");
            map.put("elasticseachHost", "");
            map.put("elasticseachPort", "");

            runProcess(kubeconfig, map, "delete.sh");
            return "distributed tracing deleted...";
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
                "dataroaster-jaeger.yaml"
        );
        for (String file : files) {
            Map<String, String> kv = new HashMap<>();
            kv.put("tempDirectory", tempDirectory);
            kv.put("kubeconfig", "kubeconfig");
            kv.put("namespace", K8sNamespace.DEFAULT_NAMESPACE_JAEGER);
            kv.put("storageClass", (String) map.get("storageClass"));
            kv.put("ingressHost", (String) map.get("ingressHost"));
            kv.put("elasticsearchHost", (String) map.get("elasticsearchHost"));
            kv.put("elasticsearchPort", (String) map.get("elasticsearchPort"));

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
