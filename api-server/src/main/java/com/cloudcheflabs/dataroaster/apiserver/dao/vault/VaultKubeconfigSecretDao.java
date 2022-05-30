package com.cloudcheflabs.dataroaster.apiserver.dao.vault;

import com.cloudcheflabs.dataroaster.apiserver.dao.common.AbstractVaultDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import org.springframework.stereotype.Repository;
import org.springframework.vault.support.VaultResponseSupport;

@Repository
public class VaultKubeconfigSecretDao extends AbstractVaultDao<Kubeconfig> {
    @Override
    public Kubeconfig readSecret(String path, Class<Kubeconfig> clazz) {
        VaultResponseSupport<Kubeconfig> response = vaultTemplate.read(path, clazz);
        return response.getData();
    }
}
