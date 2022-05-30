package com.cloudcheflabs.dataroaster.authorizer.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.authorizer.AuthorizerApplication;
import com.cloudcheflabs.dataroaster.authorizer.component.FavreBcryptPasswordEncoder;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {AuthorizerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class TokenPasswordValidatorTest {

    private static Logger LOG = LoggerFactory.getLogger(TokenPasswordValidatorTest.class);

    @Autowired
    private TokenStore tokenStore;


    @Test
    public void printClaims() throws Exception
    {
        String accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1NjY0Mzc5NDgsInVzZXJfbmFtZSI6Im15a2lkb25nQGdtYWlsLmNvbSIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJqdGkiOiI5MjQ0ZjQyOC1mNDAyLTRmZDMtYmE0ZC05ZWE1ZTk0OTUxNmEiLCJjbGllbnRfaWQiOiJjb25zb2xlIiwic2NvcGUiOlsicmVhZCJdfQ.O2tCyaW_4XnoSOnoFk0WNv0JxSh4Zl6ATSu5pWjFRKNw6K_Nk_sJTfHcBia2Vyjv28WTnVRMorgsr_rlVR7_N68tFdMkm65AxRdJ0mT9fXPmtqCZJtBWXf89KZAWls0V8hxBiMipq4RZKi2vq1NLST5teg3rHZdyXyOkm7v_9VVZdqOsp-K15tBy5iY2l8RSTSGSLwseoejQwx2QPVIjksiWFT9X5iMAIFBTQhpdj-7ofvUwrpqRE8zBXeslEP4YznIlI6-HMdYHBmGNzCKH309Y2n-sQmXHd69m_9ctUiKaovb6ZjCSZd-iziyU7ai9ldlJzTOtvO71WXUOxmak5A";

        ObjectMapper objectMapper = new ObjectMapper();

        JsonParser parser = JsonParserFactory.getJsonParser();
        Map<String, ?> tokenData = parser.parseMap(JwtHelper.decode(accessToken).getClaims());

        LOG.info(JsonWriter.formatJson(objectMapper.writeValueAsString(tokenData)));
    }


    @Test
    public void checkPasswordWithPasswordEncoder()
    {
        String passwordHash = "$2b$08$VIUdrthjhuI6lGtOHuCtt.ef1gf8.YqzZf8Rl/rE2XYYFXj6iA3r6";
        String password = "icarus0337!";

        DelegatingPasswordEncoder delegatingPasswordEncoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(new FavreBcryptPasswordEncoder());

        boolean matched = delegatingPasswordEncoder.matches(password, passwordHash);
        System.out.println("matched: " + matched);
    }


    @Test
    public void checkPassword()
    {
        String passwordHash = "$2b$08$VIUdrthjhuI6lGtOHuCtt.ef1gf8.YqzZf8Rl/rE2XYYFXj6iA3r6";
        String password = "icarus0337!";

        BCrypt.Result result = BCrypt.verifyer().verify(password.getBytes(), passwordHash.getBytes());

        System.out.println("matched: " + result.verified);
    }
}
