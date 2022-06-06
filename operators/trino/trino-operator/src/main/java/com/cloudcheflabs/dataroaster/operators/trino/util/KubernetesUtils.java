package com.cloudcheflabs.dataroaster.operators.trino.util;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.operators.trino.config.TrinoConfiguration;
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
            LOG.warn("instead return default trino operator namespace [{}]", TrinoConfiguration.DEFAULT_TRINO_OPERATOR_NAMESPACE);
            return TrinoConfiguration.DEFAULT_TRINO_OPERATOR_NAMESPACE;
        }
    }
}
