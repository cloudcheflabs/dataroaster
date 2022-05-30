package com.cloudcheflabs.dataroaster.cli.domain;

public class ConfigProps {

    private String server;
    private String accessToken;

    public ConfigProps() {}

    public ConfigProps(String server, String accessToken) {
        this.server = server;
        this.accessToken = accessToken;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getServer() {
        return server;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
