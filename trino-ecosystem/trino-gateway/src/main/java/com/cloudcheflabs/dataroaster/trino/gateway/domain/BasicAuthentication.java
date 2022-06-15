package com.cloudcheflabs.dataroaster.trino.gateway.domain;

public class BasicAuthentication {
    private String user;
    private String password;

    public BasicAuthentication(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
