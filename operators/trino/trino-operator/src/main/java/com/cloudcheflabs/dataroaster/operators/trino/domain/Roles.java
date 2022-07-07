package com.cloudcheflabs.dataroaster.operators.trino.domain;

public enum Roles {
    ROLE_PLATFORM_ADMIN(1000), ROLE_USER(10);

    private int level;

    private Roles(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }
}