package com.cloudcheflabs.dataroaster.authorizer.component;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created by mykidong on 2019-08-21.
 */
public class FavreBcryptPasswordEncoder implements PasswordEncoder {


    private static final int DEFAULT_COST = 8;
    private int cost;

    public FavreBcryptPasswordEncoder()
    {
        this(DEFAULT_COST);
    }

    public FavreBcryptPasswordEncoder(int cost)
    {
        this.cost = cost;
    }


    @Override
    public String encode(CharSequence rawPassword) {

        String bcryptHashString = BCrypt.withDefaults().hashToString(this.cost, rawPassword.toString().toCharArray());

        return bcryptHashString;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {

        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toString().getBytes(), encodedPassword.getBytes());

        return result.verified;
    }
}
