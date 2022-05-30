package com.cloudcheflabs.dataroaster.apiserver.dao.common;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.SecretDao;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;


public abstract class AbstractVaultDao<T> implements SecretDao<T>, InitializingBean{

    @Value("${vault.host}")
    private String host;

    @Value("${vault.port}")
    private int port;

    @Value("${vault.trustStore}")
    private String trustStore;

    @Value("${vault.token}")
    private String token;

    protected VaultTemplate vaultTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.setProperty("javax.net.ssl.trustStore", trustStore);

        vaultTemplate = new VaultTemplate(VaultEndpoint.create(host, port),
                new TokenAuthentication(token));
    }

    @Override
    public void writeSecret(String path, Object value) {
        vaultTemplate.write(path, value);
    }

    @Override
    public abstract T readSecret(String path, Class<T> clazz);

    @Override
    public void delete(String path) {
        vaultTemplate.delete(path);
    }
}
