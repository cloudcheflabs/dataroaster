storage "mysql" {
  address  = "127.0.0.1:3306"
  database = "vault"
  table    = "vault_data"
  username = "{{ mysql_vault_user }}"
  password = "{{ mysql_vault_password }}"
}

listener "tcp" {
  address = "0.0.0.0:8200"
  tls_cert_file = "{{ vault_conf_dir }}/localhost.cert.pem"
  tls_key_file = "{{ vault_conf_dir }}/localhost.decrypted.key.pem"
}

disable_mlock = true