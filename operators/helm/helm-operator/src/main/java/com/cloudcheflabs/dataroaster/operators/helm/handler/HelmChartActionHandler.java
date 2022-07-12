package com.cloudcheflabs.dataroaster.operators.helm.handler;

import com.cloudcheflabs.dataroaster.operators.helm.crd.HelmChart;
import com.cloudcheflabs.dataroaster.operators.helm.crd.HelmChartSpec;
import com.cloudcheflabs.dataroaster.operators.helm.util.FileUtils;
import com.cloudcheflabs.dataroaster.operators.helm.util.HelmProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelmChartActionHandler implements ActionHandler<HelmChart> {

    private static Logger LOG = LoggerFactory.getLogger(HelmChartActionHandler.class);


    public HelmChartActionHandler() {
    }


    @Override
    public void create(HelmChart helmChart) {
        try {
            HelmChartSpec spec = helmChart.getSpec();
            String chartName = spec.getChartName();
            String values = spec.getValues();

            String tempDirectory = FileUtils.createHelmTempDirectory();
            String valuesFile = "custom-values.yaml";
            String valuesFilePath = tempDirectory + "/" + valuesFile;
            if (values != null) {
                // create values yaml.
                com.cloudcheflabs.dataroaster.common.util.FileUtils.stringToFile(values, valuesFilePath, false);
                LOG.info("custom-values.yaml: \n{}", com.cloudcheflabs.dataroaster.common.util.FileUtils.fileToString(valuesFilePath, false));
            }


            StringBuffer cmd = new StringBuffer();
            cmd.append("helm repo add ").append(chartName).append(" ").append(spec.getRepo()).append("\n");
            cmd.append("helm repo update").append("\n").append("\n");

            cmd.append("helm install ").append("\\").append("\n");
            cmd.append(spec.getName()).append(" ").append("\\").append("\n");
            cmd.append("--create-namespace ").append("\\").append("\n");
            cmd.append("--namespace ").append(spec.getNamespace()).append(" ").append("\\").append("\n");
            cmd.append("--version ").append(spec.getVersion()).append(" ").append("\\").append("\n");
            if (values != null) {
                cmd.append("--values ").append(valuesFilePath).append(" ").append("\\").append("\n");
            }
            cmd.append(chartName).append("/").append(chartName);

            String runHelmShell = "run-helm.sh";
            String runHelmShellPath = tempDirectory + "/" + runHelmShell;
            // create run helm shell.
            com.cloudcheflabs.dataroaster.common.util.FileUtils.stringToFile(cmd.toString(), runHelmShellPath, true);
            LOG.info("run-helm.sh: \n{}", com.cloudcheflabs.dataroaster.common.util.FileUtils.fileToString(runHelmShellPath, false));

            // run helm shell.
            HelmProcessExecutor helmProcessExecutor = new HelmProcessExecutor();
            helmProcessExecutor.doExec(runHelmShellPath);

            FileUtils.deleteDirectory(tempDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void upgrade(HelmChart helmChart) {
        try {
            HelmChartSpec spec = helmChart.getSpec();
            String chartName = spec.getChartName();
            String values = spec.getValues();

            String tempDirectory = FileUtils.createHelmTempDirectory();
            String valuesFile = "custom-values.yaml";
            String valuesFilePath = tempDirectory + "/" + valuesFile;
            if (values != null) {
                // create values yaml.
                com.cloudcheflabs.dataroaster.common.util.FileUtils.stringToFile(values, valuesFilePath, false);
                LOG.info("custom-values.yaml: \n{}", com.cloudcheflabs.dataroaster.common.util.FileUtils.fileToString(valuesFilePath, false));
            }

            StringBuffer cmd = new StringBuffer();
            cmd.append("helm repo add ").append(chartName).append(" ").append(spec.getRepo()).append("\n");
            ;
            cmd.append("helm repo update").append("\n").append("\n");
            ;

            cmd.append("helm upgrade ").append("\\").append("\n");
            ;
            cmd.append(spec.getName()).append(" ").append("\\").append("\n");
            ;
            cmd.append("--namespace ").append(spec.getNamespace()).append(" ").append("\\").append("\n");
            ;
            cmd.append("--version ").append(spec.getVersion()).append(" ").append("\\").append("\n");
            ;
            if (values != null) {
                cmd.append("--values ").append(valuesFilePath).append(" ").append("\\").append("\n");
                ;
            }
            cmd.append(chartName).append("/").append(chartName);

            String runHelmShell = "run-helm.sh";
            String runHelmShellPath = tempDirectory + "/" + runHelmShell;
            // create run helm shell.
            com.cloudcheflabs.dataroaster.common.util.FileUtils.stringToFile(cmd.toString(), runHelmShellPath, true);
            LOG.info("run-helm.sh: \n{}", com.cloudcheflabs.dataroaster.common.util.FileUtils.fileToString(runHelmShellPath, false));

            // run helm shell.
            HelmProcessExecutor helmProcessExecutor = new HelmProcessExecutor();
            helmProcessExecutor.doExec(runHelmShellPath);


            FileUtils.deleteDirectory(tempDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void destroy(HelmChart helmChart) {
        try {
            HelmChartSpec spec = helmChart.getSpec();
            String chartName = spec.getChartName();

            String tempDirectory = FileUtils.createHelmTempDirectory();

            StringBuffer cmd = new StringBuffer();
            cmd.append("helm repo add ").append(chartName).append(" ").append(spec.getRepo()).append("\n");
            ;
            cmd.append("helm repo update").append("\n").append("\n");
            ;

            cmd.append("helm uninstall ").append("\\").append("\n");
            ;
            cmd.append(spec.getName()).append(" ").append("\\").append("\n");
            ;
            cmd.append("--namespace ").append(spec.getNamespace());

            String runHelmShell = "run-helm.sh";
            String runHelmShellPath = tempDirectory + "/" + runHelmShell;
            // create run helm shell.
            com.cloudcheflabs.dataroaster.common.util.FileUtils.stringToFile(cmd.toString(), runHelmShellPath, true);
            LOG.info("run-helm.sh: \n{}", com.cloudcheflabs.dataroaster.common.util.FileUtils.fileToString(runHelmShellPath, false));

            // run helm shell.
            HelmProcessExecutor helmProcessExecutor = new HelmProcessExecutor();
            helmProcessExecutor.doExec(runHelmShellPath);


            FileUtils.deleteDirectory(tempDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
