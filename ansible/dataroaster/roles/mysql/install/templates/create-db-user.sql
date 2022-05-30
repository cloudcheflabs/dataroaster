CREATE DATABASE vault;
CREATE USER '{{ mysql_vault_user }}'@'localhost' IDENTIFIED BY '{{ mysql_vault_password }}';
GRANT ALL PRIVILEGES ON *.* TO '{{ mysql_vault_user }}'@'localhost' WITH GRANT OPTION;
flush privileges;

CREATE DATABASE dataroaster;
CREATE USER '{{ mysql_dataroaster_user }}'@'localhost' IDENTIFIED BY '{{ mysql_dataroaster_password }}';
GRANT ALL PRIVILEGES ON *.* TO '{{ mysql_dataroaster_user }}'@'localhost' WITH GRANT OPTION;
flush privileges;