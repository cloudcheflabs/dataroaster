package com.cloudcheflabs.dataroaster.operators.dataroaster.controller;

import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.Privileges;
import com.cloudcheflabs.dataroaster.operators.dataroaster.domain.Roles;
import com.cloudcheflabs.dataroaster.operators.dataroaster.filter.AuthorizationFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class RoleUtils {
    public static int getMaxRoleLevel(HttpServletRequest context) {
        Privileges privileges = (Privileges) context.getAttribute(AuthorizationFilter.KEY_PRIVILEGES);

        List<String> roleList = privileges.getRoleList();
        int maxRoleLevel = -1;
        for(String role : roleList) {
            for(Roles roles : Roles.values()) {
                String roleName = roles.name();
                if(role.equals(roleName)) {
                    int rolesLevel = roles.getLevel();
                    maxRoleLevel = (maxRoleLevel < rolesLevel) ? rolesLevel : maxRoleLevel;
                }
            }
        }
        return maxRoleLevel;
    }

    public static Roles getMaxRole(int roleLevel) {
        Roles maxRole = null;
        for(Roles roles : Roles.values()) {
            if(roles.getLevel() == roleLevel) {
                maxRole = roles;

                break;
            }
        }

        return maxRole;
    }
}
