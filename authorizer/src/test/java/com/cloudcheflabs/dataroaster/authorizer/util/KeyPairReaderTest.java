package com.cloudcheflabs.dataroaster.authorizer.util;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;

/**
 * Created by mykidong on 2019-08-21.
 */
public class KeyPairReaderTest {

    @Test
    public void readPublicKey() throws Exception
    {
        Resource resource = new ClassPathResource("authorizer.pub");
        String publicKey = null;
        try {
            publicKey = IOUtils.toString(resource.getInputStream());

            System.out.println("publicKey: [" + publicKey + "]");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void readKeyPair() throws Exception
    {
        Resource resource = new ClassPathResource("authorizer.jks");

        String alias = "authorizer";
        String password = "icarus";


        KeyStore store = KeyStore.getInstance("jks");
        store.load(resource.getInputStream(), password.toCharArray());

        RSAPrivateCrtKey key = (RSAPrivateCrtKey) store.getKey(alias, password.toCharArray());
        RSAPublicKeySpec spec = new RSAPublicKeySpec(key.getModulus(), key.getPublicExponent());
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
