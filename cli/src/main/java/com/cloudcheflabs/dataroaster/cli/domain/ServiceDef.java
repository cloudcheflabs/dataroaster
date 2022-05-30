package com.cloudcheflabs.dataroaster.cli.domain;

public class ServiceDef {
    public static enum ServiceTypeEnum {
        INGRESS_CONTROLLER,
        POD_LOG_MONITORING,
        DISTRIBUTED_TRACING,
        METRICS_MONITORING,
        PRIVATE_REGISTRY,
        CI_CD,
        BACKUP,
        SECRET_MANAGEMENT,
        STORAGE,
        DATA_CATALOG,
        QUERY_ENGINE,
        STREAMING,
        ANALYTICS,
        WORKFLOW
    }
}
