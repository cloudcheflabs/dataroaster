package com.cloudcheflabs.dataroaster.apiserver.domain.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_authorities")
public class UserAuthorities implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "authority")
    private String authority;

    @ManyToOne
    @JoinColumn(name ="user_id")
    private Users users;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }
}
