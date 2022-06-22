package com.cloudcheflabs.dataroaster.operators.dataroaster.config;

import com.cloudcheflabs.dataroaster.operators.dataroaster.api.service.UsersService;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model.Users;
import com.cloudcheflabs.dataroaster.operators.dataroaster.util.BCryptUtils;
import com.cloudcheflabs.dataroaster.operators.dataroaster.util.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminUserConfigurer {

    private static Logger LOG = LoggerFactory.getLogger(AdminUserConfigurer.class);

    @Autowired
    private UsersService usersService;

    @Bean
    public String createAdminUser() {
        Users users = usersService.findOne("admin");
        if(users == null) {
            String randomPassword = RandomUtils.randomPassword();
            LOG.info("randomly generated password for user 'admin': {}", randomPassword);
            String bcryptedPassword = BCryptUtils.encodeWithBCrypt(randomPassword);

            users = new Users();
            users.setUser("admin");
            users.setPassword(bcryptedPassword);
            usersService.create(users);
            LOG.info("user admin created.");
        }

        return "admin user created if not already exist";
    }


}
