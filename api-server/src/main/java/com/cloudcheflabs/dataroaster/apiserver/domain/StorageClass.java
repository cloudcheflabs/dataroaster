package com.cloudcheflabs.dataroaster.apiserver.domain;

public class StorageClass {

    private String name;
    private String provisioner;
    private String reclaimPolicy;
    private String volumeBindingMode;

    public StorageClass(String name,
                        String provisioner,
                        String reclaimPolicy,
                        String volumeBindingMode) {
        this.name = name;
        this.provisioner = provisioner;
        this.reclaimPolicy = reclaimPolicy;
        this.volumeBindingMode = volumeBindingMode;
    }

    public StorageClass() {}

    public String getName() {
        return name;
    }

    public String getProvisioner() {
        return provisioner;
    }

    public String getReclaimPolicy() {
        return reclaimPolicy;
    }

    public String getVolumeBindingMode() {
        return volumeBindingMode;
    }
}
