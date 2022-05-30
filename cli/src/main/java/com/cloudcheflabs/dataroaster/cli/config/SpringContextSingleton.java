package com.cloudcheflabs.dataroaster.cli.config;


import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringContextSingleton {

    private static ApplicationContext applicationContext;

    private static final Object lock = new Object();

    public static ApplicationContext getInstance()
    {
        if(applicationContext == null) {
            synchronized(lock) {
                if(applicationContext == null) {
                    applicationContext = new AnnotationConfigApplicationContext(APIConfig.class);
                }
            }
        }

        return applicationContext;
    }
}
