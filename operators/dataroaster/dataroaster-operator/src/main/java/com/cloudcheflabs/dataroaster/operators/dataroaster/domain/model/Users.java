package com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model;

import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "users")
public class Users implements Serializable {

    @Id
    @Column(name = "user")
    private String user;

    @Column(name = "password")
    private String password;

    @OneToMany(mappedBy = "users", fetch = FetchType.EAGER)
    private Set<UserToken> userTokenSet = Sets.newHashSet();


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

    public Set<UserToken> getUserTokenSet() {
        return userTokenSet;
    }

    public void setUserTokenSet(Set<UserToken> userTokenSet) {
        this.userTokenSet = userTokenSet;
    }
}
