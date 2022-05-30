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

public class WorkflowHandler {

    private static Logger LOG = LoggerFactory.getLogger(WorkflowHandler.class);

    private static String moveFiles(Kubeconfig kubeconfig) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        String formattedDate = fmt.print(DateTime.now());
        String uid = formattedDate + "-" + UUID.randomUUID().toString();
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/workflow/" + uid;

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        String rootPath = "/templates/workflow";
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath, "1.0.0", tempDirectory);
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0", "templates", tempDirectory + "/templates");
     

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
            return "workflow created...";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static String delete(Kubeconfig kubeconfig) {
        try {
            Map<String, Object> map = new HashMap<>();

            runProcess(kubeconfig, map, "delete.sh");
            return "workflow deleted...";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static void runProcess(Kubeconfig kubeconfig, Map<String, ?> map, String runShell) {
        String tempDirectory = moveFiles(kubeconfig);

        // root dir files.
        List<String> files = Arrays.asList(
                "create.sh",
                "dataroaster-values.yaml",
                "delete.sh"
        );

        for (String file : files) {
            Map<String, Object> kv = new HashMap<>();
            kv.put("tempDirectory", tempDirectory);
            kv.put("kubeconfig", "kubeconfig");
            kv.put("namespace", K8sNamespace.DEFAULT_NAMESPACE_ARGO_WORKFLOW);
            kv.put("storageClass", (String) map.get("storageClass"));
            kv.put("storageSize", (Integer) map.get("storageSize"));
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
