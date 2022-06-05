package com.cloudcheflabs.dataroaster.operators.trino.crd;

import java.util.List;

public class SecurityContext {
    private int fsGroup;
    private int runAsGroup;
    private boolean runAsNonRoot;
    private int runAsUser;
    private SeLinuxOptions seLinuxOptions;
    private List<Integer> supplementalGroups;
    private List<Sysctl> sysctls;

    public int getFsGroup() {
        return fsGroup;
    }

    public void setFsGroup(int fsGroup) {
        this.fsGroup = fsGroup;
    }

    public int getRunAsGroup() {
        return runAsGroup;
    }

    public void setRunAsGroup(int runAsGroup) {
        this.runAsGroup = runAsGroup;
    }

    public boolean isRunAsNonRoot() {
        return runAsNonRoot;
    }

    public void setRunAsNonRoot(boolean runAsNonRoot) {
        this.runAsNonRoot = runAsNonRoot;
    }

    public int getRunAsUser() {
        return runAsUser;
    }

    public void setRunAsUser(int runAsUser) {
        this.runAsUser = runAsUser;
    }

    public SeLinuxOptions getSeLinuxOptions() {
        return seLinuxOptions;
    }

    public void setSeLinuxOptions(SeLinuxOptions seLinuxOptions) {
        this.seLinuxOptions = seLinuxOptions;
    }

    public List<Integer> getSupplementalGroups() {
        return supplementalGroups;
    }

    public void setSupplementalGroups(List<Integer> supplementalGroups) {
        this.supplementalGroups = supplementalGroups;
    }

    public List<Sysctl> getSysctls() {
        return sysctls;
    }

    public void setSysctls(List<Sysctl> sysctls) {
        this.sysctls = sysctls;
    }

    public static class WindowsOptions {
        private String gmsaCredentialSpec;
        private String gmsaCredentialSpecName;
        private String runAsUserName;

        public String getGmsaCredentialSpec() {
            return gmsaCredentialSpec;
        }

        public void setGmsaCredentialSpec(String gmsaCredentialSpec) {
            this.gmsaCredentialSpec = gmsaCredentialSpec;
        }

        public String getGmsaCredentialSpecName() {
            return gmsaCredentialSpecName;
        }

        public void setGmsaCredentialSpecName(String gmsaCredentialSpecName) {
            this.gmsaCredentialSpecName = gmsaCredentialSpecName;
        }

        public String getRunAsUserName() {
            return runAsUserName;
        }

        public void setRunAsUserName(String runAsUserName) {
            this.runAsUserName = runAsUserName;
        }
    }

    public static class Sysctl {
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class SeLinuxOptions {
        private String level;
        private String role;
        private String type;
        private String user;

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }
}
