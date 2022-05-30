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

public class AnalyticsHandler {

    private static Logger LOG = LoggerFactory.getLogger(AnalyticsHandler.class);

    private static String moveFiles(Kubeconfig kubeconfig) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        String formattedDate = fmt.print(DateTime.now());
        String uid = formattedDate + "-" + UUID.randomUUID().toString();
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/analytics/" + uid;

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        String rootPath = "/templates/analytics";
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath, "1.0.0", tempDirectory);
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0", "jupyterhub", tempDirectory + "/jupyterhub");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0/jupyterhub", "templates", tempDirectory + "/jupyterhub/templates");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0", "redash", tempDirectory + "/redash");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0/redash", "templates", tempDirectory + "/redash/templates");
       
        // kubeconfig raw yaml.
        String kubeconfigName = "kubeconfig";
        String kubeconfigString = kubeconfig.getRawKubeconfig();

        // write kubeconfig file.
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/jupyterhub/" + kubeconfigName, false);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/redash/" + kubeconfigName, false);
      
        return tempDirectory;
    }

    public static String create(Kubeconfig kubeconfig, Map<String, ?> map) {
        try {
            runProcess(kubeconfig, map, "create.sh");
            return "analytics created...";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static String delete(Kubeconfig kubeconfig) {
        try {
            Map<String, Object> map = new HashMap<>();

            runProcess(kubeconfig, map, "delete.sh");
            return "analytics deleted...";
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

        // jupyterhub.
        files = Arrays.asList(
                "create.sh",
                "delete.sh"
        );

        for (String file : files) {
            Map<String, Object> kv = new HashMap<>();
            kv.put("kubeconfig", "kubeconfig");
            kv.put("jupyterhubNamespace", K8sNamespace.DEFAULT_NAMESPACE_JUPYTERHUB);
            kv.put("jupyterhubGithubClientId", (String) map.get("jupyterhubGithubClientId"));
            kv.put("jupyterhubGithubClientSecret", (String) map.get("jupyterhubGithubClientSecret"));
            kv.put("jupyterhubIngressHost", (String) map.get("jupyterhubIngressHost"));
            kv.put("storageClass", (String) map.get("storageClass"));
            kv.put("jupyterhubStorageSize", (Integer) map.get("jupyterhubStorageSize"));

            TemplateUtils.toFile(tempDirectory + "/jupyterhub/" + file,
                    false,
                    kv,
                    tempDirectory + "/jupyterhub/" + file,
                    true);
        }

        // redash.
        files = Arrays.asList(
                "create.sh",
                "delete.sh",
                "dataroaster-values.yaml"
        );

        for (String file : files) {
            Map<String, Object> kv = new HashMap<>();
            kv.put("kubeconfig", "kubeconfig");
            kv.put("redashNamespace", K8sNamespace.DEFAULT_NAMESPACE_REDASH);
            kv.put("storageClass", (String) map.get("storageClass"));
            kv.put("redashStorageSize", (Integer) map.get("redashStorageSize"));

            TemplateUtils.toFile(tempDirectory + "/redash/" + file,
                    false,
                    kv,
                    tempDirectory + "/redash/" + file,
                    true);
        }

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + runShell);

        // delete temp directory.
        //FileUtils.deleteDirectory(tempDirectory);
    }
}
