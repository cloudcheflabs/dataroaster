package com.cloudcheflabs.dataroaster.operators.spark.util;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.operators.spark.config.SparkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesUtils {
    private static Logger LOG = LoggerFactory.getLogger(KubernetesUtils.class);

    public static String getNamespace() {
        try {
            String namespaceFile = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";
            return FileUtils.fileToString(namespaceFile, false);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.warn("instead return default spark operator namespace [{}]", SparkConfiguration.DEFAULT_SPARK_OPERATOR_NAMESPACE);
            return SparkConfiguration.DEFAULT_SPARK_OPERATOR_NAMESPACE;
        }
    }
}
