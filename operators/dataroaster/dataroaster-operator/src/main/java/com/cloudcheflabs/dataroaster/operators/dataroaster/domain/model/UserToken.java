package com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_token")
public class UserToken implements Serializable {

    public static final int EXPIRATION_IN_HOUR = 10;

    @Id
    @Column(name = "token")
    private String token;

    @Column(name = "expiration")
    private long expiration;

    @ManyToOne
    @JoinColumn(name ="user")
    private Users users;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }
}
