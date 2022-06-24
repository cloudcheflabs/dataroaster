-- create db and user.
CREATE DATABASE if not exists dataroaster;

CREATE USER if not exists 'admin'@'%' IDENTIFIED BY 'Admin123!';
GRANT ALL PRIVILEGES ON dataroaster.* TO 'admin'@'%' WITH GRANT OPTION;
flush privileges;

CREATE USER if not exists 'admin'@'localhost' IDENTIFIED BY 'Admin123!';
GRANT ALL PRIVILEGES ON dataroaster.* TO 'admin'@'localhost' WITH GRANT OPTION;
flush privileges;

-- user
create table if not exists dataroaster.users
(
    `user`  varchar(100) NOT NULL,
    `password`  varchar(1000) NOT NULL,
    PRIMARY KEY (`user`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- user token
create table if not exists dataroaster.user_token
(
    `token`  varchar(200) NOT NULL,
    `expiration` bigint(11) unsigned NOT NULL,
    `user`  varchar(100) NOT NULL,
    PRIMARY KEY (`token`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `dataroaster`.`user_token`
    ADD CONSTRAINT `users_user_token`
        FOREIGN KEY (`user`) REFERENCES `dataroaster`.`users` (`user`);


-- data platform components.
create table if not exists dataroaster.components
(
    `comp_name`  varchar(100) NOT NULL,
    PRIMARY KEY (`comp_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- custom resource
create table if not exists dataroaster.custom_resource
(
    `id` varchar(200) NOT NULL,
    `kind`  varchar(100) NOT NULL,
    `name`  varchar(100) NOT NULL,
    `namespace`  varchar(100) NOT NULL,
    `yaml` text NOT NULL,
    `comp_name`  varchar(100) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `dataroaster`.`custom_resource`
    ADD CONSTRAINT `custom_resource_components`
        FOREIGN KEY (`comp_name`) REFERENCES `dataroaster`.`components` (`comp_name`);