package com.cloudcheflabs.dataroaster.trino.gateway.controller;

import com.cloudcheflabs.dataroaster.trino.gateway.domain.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.Callable;

public class ControllerUtils {

    private static Logger LOG = LoggerFactory.getLogger(ControllerUtils.class);

    public static String successMessage() {
        return "{ 'result': 'SUCCESS'}";
    }

    public static String doProcess(Roles roles, HttpServletRequest context, Callable<String> task) {
        // role level.
        int allowedRoleLevel = roles.getLevel();

        // max role level of request user.
        int maxRoleLevel = RoleUtils.getMaxRoleLevel(context);

        if(maxRoleLevel >= allowedRoleLevel) {
            try {
                return task.call();
            } catch (Exception e) {

                e.printStackTrace();
                LOG.info("instanceof " + e.getClass());

                if(e instanceof ResponseStatusException) {
                    throw (ResponseStatusException) e;
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "NOT ALLOWED: NO PRIVILEGES");
        }
    }
}
