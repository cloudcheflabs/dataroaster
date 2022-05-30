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

public class IngressControllerHandler {

    private static Logger LOG = LoggerFactory.getLogger(IngressControllerHandler.class);

    private static String moveFiles(Kubeconfig kubeconfig) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        String formattedDate = fmt.print(DateTime.now());
        String uid = formattedDate + "-" + UUID.randomUUID().toString();
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/ingress-controller/" + uid;

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        String rootPath = "/templates/ingress-controller";
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath, "1.0.0", tempDirectory);
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0", "cert-manager", tempDirectory + "/cert-manager");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0", "ingress-controller-nginx", tempDirectory + "/ingress-controller-nginx");

        // kubeconfig raw yaml.
        String kubeconfigName = "kubeconfig";
        String kubeconfigString = kubeconfig.getRawKubeconfig();

        // write kubeconfig file.
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/cert-manager/" + kubeconfigName, false);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/ingress-controller-nginx/" + kubeconfigName, false);

        return tempDirectory;
    }

    public static String create(Kubeconfig kubeconfig) {
        try {
            runProcess(kubeconfig, "create.sh");
            return "ingress controller created...";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static String delete(Kubeconfig kubeconfig) {
        try {
            runProcess(kubeconfig, "delete.sh");
            return "ingress controller deleted...";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static void runProcess(Kubeconfig kubeconfig, String runShell) {
        String tempDirectory = moveFiles(kubeconfig);

        List<String> files = Arrays.asList(
                "create.sh",
                "delete.sh"
        );

        // root dir files.
        for (String file : files) {
            Map<String, String> kv = new HashMap<>();
            kv.put("tempDirectory", tempDirectory);

            TemplateUtils.toFile(tempDirectory + "/" + file,
                    false,
                    kv,
                    tempDirectory + "/" + file,
                    true);
        }

        // cert-manager.
        for (String file : files) {
            Map<String, String> kv = new HashMap<>();
            kv.put("kubeconfig", "kubeconfig");
            kv.put("namespace", K8sNamespace.DEFAULT_NAMESPACE_CERT_MANAGER);

            TemplateUtils.toFile(tempDirectory + "/cert-manager/" + file,
                    false,
                    kv,
                    tempDirectory + "/cert-manager/" + file,
                    true);
        }

        // ingress controller nginx.
        files = Arrays.asList(
                "create.sh",
                "delete.sh"
        );

        for (String file : files) {
            Map<String, String> kv = new HashMap<>();
            kv.put("kubeconfig", "kubeconfig");
            kv.put("namespace", K8sNamespace.DEFAULT_NAMESPACE_INGRESS_CONTROLLER_NGINX);

            TemplateUtils.toFile(tempDirectory + "/ingress-controller-nginx/" + file,
                    false,
                    kv,
                    tempDirectory + "/ingress-controller-nginx/" + file,
                    true);
        }

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + runShell);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);
    }
}
