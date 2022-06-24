package com.cloudcheflabs.dataroaster.operators.dataroaster.domain.model;

import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "components")
public class Components implements Serializable {

    @Id
    @Column(name = "comp_name")
    private String compName;

    @OneToMany(mappedBy = "components", fetch = FetchType.EAGER)
    private Set<CustomResource> customResourceSet = Sets.newHashSet();

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    public Set<CustomResource> getCustomResourceSet() {
        return customResourceSet;
    }

    public void setCustomResourceSet(Set<CustomResource> customResourceSet) {
        this.customResourceSet = customResourceSet;
    }
}
