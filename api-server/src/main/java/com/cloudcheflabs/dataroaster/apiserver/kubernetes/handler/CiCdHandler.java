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

public class CiCdHandler {

    private static Logger LOG = LoggerFactory.getLogger(CiCdHandler.class);

    private static String moveFiles(Kubeconfig kubeconfig) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        String formattedDate = fmt.print(DateTime.now());
        String uid = formattedDate + "-" + UUID.randomUUID().toString();
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/ci-cd/" + uid;

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        String rootPath = "/templates/ci-cd";
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath, "1.0.0", tempDirectory);
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0", "argocd", tempDirectory + "/argocd");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0/argocd", "templates", tempDirectory + "/argocd/templates");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0", "jenkins", tempDirectory + "/jenkins");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0/jenkins", "templates", tempDirectory + "/jenkins/templates");

        // kubeconfig raw yaml.
        String kubeconfigName = "kubeconfig";
        String kubeconfigString = kubeconfig.getRawKubeconfig();

        // write kubeconfig file.
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/argocd/" + kubeconfigName, false);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/jenkins/" + kubeconfigName, false);

        return tempDirectory;
    }

    public static String create(Kubeconfig kubeconfig, Map<String, ?> map) {
        try {
            runProcess(kubeconfig, map, "create.sh");
            return "ci cd created...";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static String delete(Kubeconfig kubeconfig) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("argocdNamespace", "");
            map.put("argocdIngressHost", "");
            map.put("storageClass", "");
            map.put("jenkinsNamespace", "");
            map.put("jenkinsIngressHost", "");

            runProcess(kubeconfig, map, "delete.sh");
            return "ci cd deleted...";
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
            Map<String, String> kv = new HashMap<>();
            kv.put("tempDirectory", tempDirectory);

            TemplateUtils.toFile(tempDirectory + "/" + file,
                    false,
                    kv,
                    tempDirectory + "/" + file,
                    true);
        }

        // argocd.
        files = Arrays.asList(
                "create.sh",
                "delete.sh",
                "dataroaster-values.yaml"
        );
        for (String file : files) {
            Map<String, String> kv = new HashMap<>();
            kv.put("kubeconfig", "kubeconfig");
            kv.put("argocdNamespace", K8sNamespace.DEFAULT_NAMESPACE_ARGOCD);
            kv.put("argocdIngressHost", (String) map.get("argocdIngressHost"));
            kv.put("storageClass", (String) map.get("storageClass"));

            TemplateUtils.toFile(tempDirectory + "/argocd/" + file,
                    false,
                    kv,
                    tempDirectory + "/argocd/" + file,
                    true);
        }

        // jenkins.
        files = Arrays.asList(
                "create.sh",
                "delete.sh",
                "dataroaster-values.yaml"
        );

        for (String file : files) {
            Map<String, Object> kv = new HashMap<>();
            kv.put("kubeconfig", "kubeconfig");
            kv.put("storageClass", (String) map.get("storageClass"));
            kv.put("jenkinsNamespace", K8sNamespace.DEFAULT_NAMESPACE_JENKINS);
            kv.put("jenkinsIngressHost", (String) map.get("jenkinsIngressHost"));

            TemplateUtils.toFile(tempDirectory + "/jenkins/" + file,
                    false,
                    kv,
                    tempDirectory + "/jenkins/" + file,
                    true);
        }

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + runShell);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);
    }
}
