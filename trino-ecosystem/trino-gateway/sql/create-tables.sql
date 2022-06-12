-- create db and user.
CREATE DATABASE if not exists trino_proxy;

CREATE USER if not exists 'trino'@'%' IDENTIFIED BY 'Trino123!';
GRANT ALL PRIVILEGES ON trino_proxy.* TO 'trino'@'%' WITH GRANT OPTION;
flush privileges;

CREATE USER if not exists 'trino'@'localhost' IDENTIFIED BY 'Trino123!';
GRANT ALL PRIVILEGES ON trino_proxy.* TO 'trino'@'localhost' WITH GRANT OPTION;
flush privileges;


-- trino cluster group.
create table if not exists trino_proxy.cluster_group
(
    `group_name`  varchar(100) NOT NULL,
    PRIMARY KEY (`group_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- trino cluster.
create table if not exists trino_proxy.cluster
(
    `cluster_name`   varchar(100) not null,
    `cluster_type`   varchar(100) not null,
    `url`   varchar(1000) not null,
    `activated`   boolean not null,
    `group_name`   varchar(100) NOT NULL,
    PRIMARY KEY (`cluster_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `trino_proxy`.`cluster`
    ADD CONSTRAINT `cluster_cluster_group`
        FOREIGN KEY (`group_name`) REFERENCES `trino_proxy`.`cluster_group` (`group_name`);

-- user
create table if not exists trino_proxy.users
(
    `user`  varchar(100) NOT NULL,
    `password`  varchar(1000) NOT NULL,
    `group_name`   varchar(100) NOT NULL,
    PRIMARY KEY (`user`),
    UNIQUE KEY `users_unique` (`group_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
ALTER TABLE `trino_proxy`.`users`
    ADD CONSTRAINT `users_cluster_group`
        FOREIGN KEY (`group_name`) REFERENCES `trino_proxy`.`cluster_group` (`group_name`);