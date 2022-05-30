package com.cloudcheflabs.dataroaster.authorizer.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.junit.Test;

import java.util.Base64;

/**
 * Created by mykidong on 2019-08-21.
 */
public class HashingTest {

    @Test
    public void bcrypt() throws Exception
    {
        System.out.println("helloAuthAPI: [" + BCrypt.withDefaults().hashToString(8, "helloAuthAPI".toCharArray()) + "]");

        System.out.println("dataroaster123: [" + BCrypt.withDefaults().hashToString(8, "dataroaster123".toCharArray()) + "]");
    }


    @Test
    public void base64() throws Exception
    {
        System.out.println("api:helloAuthAPI: [" + Base64.getEncoder().encodeToString("api:helloAuthAPI".getBytes()) + "]");
    }
}
