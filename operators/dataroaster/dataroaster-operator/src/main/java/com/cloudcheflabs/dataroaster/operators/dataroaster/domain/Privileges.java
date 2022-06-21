package com.cloudcheflabs.dataroaster.operators.dataroaster.domain;

import java.io.Serializable;
import java.util.List;

public class Privileges implements Serializable{

    private List<String> roleList;

    public Privileges() {}
    public Privileges(List<String> roleList) {
        this.roleList = roleList;
    }


    public List<String> getRoleList() {
        return this.roleList;
    }
}
