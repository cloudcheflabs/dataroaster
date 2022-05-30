package com.cloudcheflabs.dataroaster.operators.spark.crd;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Volume {
    private String name;
    private String type;
    private EmptyDir emptyDir;
    private HostPath hostPath;
    private PersistentVolumeClaim persistentVolumeClaim;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EmptyDir getEmptyDir() {
        return emptyDir;
    }

    public void setEmptyDir(EmptyDir emptyDir) {
        this.emptyDir = emptyDir;
    }

    public HostPath getHostPath() {
        return hostPath;
    }

    public void setHostPath(HostPath hostPath) {
        this.hostPath = hostPath;
    }

    public PersistentVolumeClaim getPersistentVolumeClaim() {
        return persistentVolumeClaim;
    }

    public void setPersistentVolumeClaim(PersistentVolumeClaim persistentVolumeClaim) {
        this.persistentVolumeClaim = persistentVolumeClaim;
    }

    public static class PersistentVolumeClaim {
        private String claimName;
        private boolean readOnly = false;

        public String getClaimName() {
            return claimName;
        }

        public void setClaimName(String claimName) {
            this.claimName = claimName;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }
    }


    public static class HostPath {
        private String path;
        private String type;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }


    public static class EmptyDir {
        private String medium;
        private Object sizeLimit;

        public String getMedium() {
            return medium;
        }

        public void setMedium(String medium) {
            this.medium = medium;
        }

        public Object getSizeLimit() {
            return sizeLimit;
        }

        public void setSizeLimit(Object sizeLimit) {
            this.sizeLimit = sizeLimit;
        }
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(new ObjectMapper(), this);
    }
}
