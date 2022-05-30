-- cluster.
create table k8s_cluster
(
    `id`       bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `cluster_name` varchar(100) not null,
    `description` varchar(1000) not null,
    PRIMARY KEY (`id`),
    UNIQUE KEY `cluster_name_unique` (`cluster_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- namespace.
create table k8s_namespace
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `namespace_name`   varchar(100) not null,
    `cluster_id`   bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `namespace_cluster_unique` (`namespace_name`, `cluster_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_namespace`
    ADD CONSTRAINT `fk_k8s_namespace_k8s_cluster`
        FOREIGN KEY (`cluster_id`) REFERENCES `k8s_cluster` (`id`);

-- kubeconfig.
create table k8s_kubeconfig
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `user_id`   bigint(11) unsigned NOT NULL,
    `cluster_id`   bigint(11) unsigned NOT NULL,
    `secret_path`   varchar(400) not null,
    PRIMARY KEY (`id`),
    UNIQUE KEY `cluster_unique` (`cluster_id`, `user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_kubeconfig`
    ADD CONSTRAINT `fk_k8s_cluster`
        FOREIGN KEY (`cluster_id`) REFERENCES `k8s_cluster` (`id`);

ALTER TABLE `k8s_kubeconfig`
    ADD CONSTRAINT `fk_k8s_kubeconfig_users`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);



-- project.
create table project
(
    `id`       bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `project_name` varchar(100) not null,
    `description` varchar(1000) not null,
    `user_id`   bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `project`
    ADD CONSTRAINT `fk_project_users`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);


-- defined service list.
create table service_def
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `type`   varchar(100) not null,
    `name`   varchar(100) not null,
    `version`   varchar(100) not null,
    `external`  boolean not null DEFAULT false,
    PRIMARY KEY (`id`),
    UNIQUE KEY `service_def_unique` (`type`, `name`, `version`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- insert service defs.
INSERT INTO service_def (`id`, `type`, `name`, `version`, `external`) VALUES (1, 'INGRESS_CONTROLLER', 'Ingress Controller NGINX, Cert-Manager', '1.0.0', false);
INSERT INTO service_def (`id`, `type`, `name`, `version`, `external`) VALUES (2, 'POD_LOG_MONITORING', 'ELK, Filebeat', '1.0.0', false);
INSERT INTO service_def (`id`, `type`, `name`, `version`, `external`) VALUES (3, 'DISTRIBUTED_TRACING', 'Jaeger', '1.0.0', false);
INSERT INTO service_def (`id`, `type`, `name`, `version`, `external`) VALUES (4, 'METRICS_MONITORING', 'Prometheus, Grafana', '1.0.0', false);
INSERT INTO service_def (`id`, `type`, `name`, `version`, `external`) VALUES (5, 'PRIVATE_REGISTRY', 'Harbor', '1.0.0', false);
INSERT INTO service_def (`id`, `type`, `name`, `version`, `external`) VALUES (6, 'CI_CD', 'Jenkins, ArgoCD', '1.0.0', false);
INSERT INTO service_def (`id`, `type`, `name`, `version`, `external`) VALUES (7, 'BACKUP', 'Velero', '1.0.0', false);
INSERT INTO service_def (`id`, `type`, `name`, `version`, `external`) VALUES (8, 'DATA_CATALOG', 'Hive Metastore', '1.0.0', false);
INSERT INTO service_def (`id`, `type`, `name`, `version`, `external`) VALUES (9, 'QUERY_ENGINE', 'Spark Thrift Server, Trino', '1.0.0', false);
INSERT INTO service_def (`id`, `type`, `name`, `version`, `external`) VALUES (10, 'STREAMING', 'Kafka', '1.0.0', false);
INSERT INTO service_def (`id`, `type`, `name`, `version`, `external`) VALUES (11, 'ANALYTICS', 'JupyterHub, Redash', '1.0.0', false);
INSERT INTO service_def (`id`, `type`, `name`, `version`, `external`) VALUES (12, 'WORKFLOW', 'Argo Workflow', '1.0.0', false);


-- services.
create table services
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `service_def_id`   bigint(11) unsigned NOT NULL,
    `namespace_id`   bigint(11) unsigned NULL,
    `project_id`   bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `services`
    ADD CONSTRAINT `fk_services_service_def`
        FOREIGN KEY (`service_def_id`) REFERENCES `service_def` (`id`);

ALTER TABLE `services`
    ADD CONSTRAINT `fk_services_project`
        FOREIGN KEY (`project_id`) REFERENCES `project` (`id`);

ALTER TABLE `services`
    ADD CONSTRAINT `fk_services_k8s_namespace`
        FOREIGN KEY (`namespace_id`) REFERENCES `k8s_namespace` (`id`);