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
        HelmChartSpec spec = helmChart.getSpec();
        String chartName = spec.getChartName();
        String values = spec.getValues();

        String tempDirectory = FileUtils.createHelmTempDirectory();
        String valuesFile = "custom-values.yaml";
        if(values != null) {
            String valuesFilePath = tempDirectory + "/" + valuesFile;
            // create values yaml.
            com.cloudcheflabs.dataroaster.common.util.FileUtils.stringToFile(values, valuesFilePath, false);
            LOG.info("custom-values.yaml: \n{}", com.cloudcheflabs.dataroaster.common.util.FileUtils.fileToString(valuesFilePath, false));
        }


        StringBuffer cmd = new StringBuffer();
        cmd.append("helm repo add ").append(chartName).append(" ").append(spec.getRepo()).append("\\");
        cmd.append("helm repo update").append("\\");
        cmd.append("helm install ").append("\\");
        cmd.append(spec.getName()).append(" ").append("\\");
        cmd.append("--create-namespace ").append("\\");
        cmd.append("--namespace ").append(spec.getNamespace()).append(" ").append("\\");
        cmd.append("--version ").append(spec.getVersion()).append(" ").append("\\");
        if(values != null) {
            cmd.append("--values ").append("./").append(valuesFile).append(" ").append("\\");
        }
        cmd.append(chartName).append("/").append(chartName).append(";");

        String runHelmShell = "run-helm.sh";
        String runHelmShellPath = tempDirectory + "/" + runHelmShell;
        // create run helm shell.
        com.cloudcheflabs.dataroaster.common.util.FileUtils.stringToFile(cmd.toString(), runHelmShellPath, true);
        LOG.info("run-helm.sh: \n{}", com.cloudcheflabs.dataroaster.common.util.FileUtils.fileToString(runHelmShellPath, false));

        // run helm shell.
        HelmProcessExecutor helmProcessExecutor = new HelmProcessExecutor();
        helmProcessExecutor.doExec(runHelmShellPath);

        //FileUtils.deleteDirectory(tempDirectory);
    }

    @Override
    public void upgrade(HelmChart helmChart) {
        HelmChartSpec spec = helmChart.getSpec();
        String chartName = spec.getChartName();
        String values = spec.getValues();

        String tempDirectory = FileUtils.createHelmTempDirectory();
        String valuesFile = "custom-values.yaml";
        if(values != null) {
            String valuesFilePath = tempDirectory + "/" + valuesFile;
            // create values yaml.
            com.cloudcheflabs.dataroaster.common.util.FileUtils.stringToFile(values, valuesFilePath, false);
            LOG.info("custom-values.yaml: \n{}", com.cloudcheflabs.dataroaster.common.util.FileUtils.fileToString(valuesFilePath, false));
        }


        StringBuffer cmd = new StringBuffer();
        cmd.append("helm repo add ").append(chartName).append(" ").append(spec.getRepo()).append("\\");
        cmd.append("helm repo update").append("\\");
        cmd.append("helm upgrade ").append("\\");
        cmd.append(spec.getName()).append(" ").append("\\");
        cmd.append("--namespace ").append(spec.getNamespace()).append(" ").append("\\");
        cmd.append("--version ").append(spec.getVersion()).append(" ").append("\\");
        if(values != null) {
            cmd.append("--values ").append("./").append(valuesFile).append(" ").append("\\");
        }
        cmd.append(chartName).append("/").append(chartName).append(";");

        String runHelmShell = "run-helm.sh";
        String runHelmShellPath = tempDirectory + "/" + runHelmShell;
        // create run helm shell.
        com.cloudcheflabs.dataroaster.common.util.FileUtils.stringToFile(cmd.toString(), runHelmShellPath, true);
        LOG.info("run-helm.sh: \n{}", com.cloudcheflabs.dataroaster.common.util.FileUtils.fileToString(runHelmShellPath, false));

        // run helm shell.
        HelmProcessExecutor helmProcessExecutor = new HelmProcessExecutor();
        helmProcessExecutor.doExec(runHelmShellPath);


        //FileUtils.deleteDirectory(tempDirectory);

    }

    @Override
    public void destroy(HelmChart helmChart) {
        HelmChartSpec spec = helmChart.getSpec();
        String chartName = spec.getChartName();

        String tempDirectory = FileUtils.createHelmTempDirectory();

        StringBuffer cmd = new StringBuffer();
        cmd.append("helm repo add ").append(chartName).append(" ").append(spec.getRepo()).append("\\");
        cmd.append("helm repo update").append("\\");
        cmd.append("helm uninstall ").append("\\");
        cmd.append(spec.getName()).append(" ").append("\\");
        cmd.append("--namespace ").append(spec.getNamespace()).append(";");

        String runHelmShell = "run-helm.sh";
        String runHelmShellPath = tempDirectory + "/" + runHelmShell;
        // create run helm shell.
        com.cloudcheflabs.dataroaster.common.util.FileUtils.stringToFile(cmd.toString(), runHelmShellPath, true);
        LOG.info("run-helm.sh: \n{}", com.cloudcheflabs.dataroaster.common.util.FileUtils.fileToString(runHelmShellPath, false));

        // run helm shell.
        HelmProcessExecutor helmProcessExecutor = new HelmProcessExecutor();
        helmProcessExecutor.doExec(runHelmShellPath);


        //FileUtils.deleteDirectory(tempDirectory);

    }
}
