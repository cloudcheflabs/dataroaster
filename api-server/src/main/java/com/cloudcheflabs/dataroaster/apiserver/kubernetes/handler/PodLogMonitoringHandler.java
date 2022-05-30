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

public class PodLogMonitoringHandler {

    private static Logger LOG = LoggerFactory.getLogger(PodLogMonitoringHandler.class);

    private static String moveFiles(Kubeconfig kubeconfig) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        String formattedDate = fmt.print(DateTime.now());
        String uid = formattedDate + "-" + UUID.randomUUID().toString();
        String tempDirectory = System.getProperty("java.io.tmpdir") + "/pod-log-monitoring/" + uid;

        // create temp. directory.
        FileUtils.createDirectory(tempDirectory);

        String rootPath = "/templates/pod-log-monitoring";
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath, "1.0.0", tempDirectory);
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0", "filebeat", tempDirectory + "/filebeat");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0/filebeat", "templates", tempDirectory + "/filebeat/templates");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0", "logstash", tempDirectory + "/logstash");
        FileUtils.copyFilesFromClasspathToFileSystem(rootPath + "/1.0.0/logstash", "templates", tempDirectory + "/logstash/templates");

        // kubeconfig raw yaml.
        String kubeconfigName = "kubeconfig";
        String kubeconfigString = kubeconfig.getRawKubeconfig();

        // write kubeconfig file.
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/filebeat/" + kubeconfigName, false);
        FileUtils.stringToFile(kubeconfigString, tempDirectory + "/logstash/" + kubeconfigName, false);

        return tempDirectory;
    }

    public static String create(Kubeconfig kubeconfig, Map<String, ?> map) {
        try {
            runProcess(kubeconfig, map, "create.sh");
            return "pod log monitoring created...";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static String delete(Kubeconfig kubeconfig) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("elasticsearchHosts", new ArrayList<String>());

            runProcess(kubeconfig, map, "delete.sh");
            return "pod log monitoring deleted...";
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

        // filebeat.
        files = Arrays.asList(
                "create.sh",
                "delete.sh",
                "dataroaster-values.yaml"
        );
        for (String file : files) {
            Map<String, String> kv = new HashMap<>();
            kv.put("kubeconfig", "kubeconfig");
            kv.put("filebeatNamespace", K8sNamespace.DEFAULT_NAMESPACE_FILEBEAT);
            kv.put("logstashNamespace", K8sNamespace.DEFAULT_NAMESPACE_LOGSTASH);

            TemplateUtils.toFile(tempDirectory + "/filebeat/" + file,
                    false,
                    kv,
                    tempDirectory + "/filebeat/" + file,
                    true);
        }

        // logstash.
        files = Arrays.asList(
                "create.sh",
                "delete.sh",
                "dataroaster-values.yaml"
        );

        for (String file : files) {
            Map<String, Object> kv = new HashMap<>();
            kv.put("kubeconfig", "kubeconfig");
            kv.put("logstashNamespace", K8sNamespace.DEFAULT_NAMESPACE_LOGSTASH);
            kv.put("elasticsearchHosts", (List) map.get("elasticsearchHosts"));

            TemplateUtils.toFile(tempDirectory + "/logstash/" + file,
                    false,
                    kv,
                    tempDirectory + "/logstash/" + file,
                    true);
        }

        // run shell.
        ProcessExecutor.doExec(tempDirectory + "/" + runShell);

        // delete temp directory.
        FileUtils.deleteDirectory(tempDirectory);
    }
}
