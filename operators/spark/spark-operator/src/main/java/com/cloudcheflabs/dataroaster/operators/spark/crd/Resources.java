package com.cloudcheflabs.dataroaster.operators.spark.crd;

public class Resources {
    private String cores;
    private String limitCores;
    private String memory;

    public String getCores() {
        return cores;
    }

    public void setCores(String cores) {
        this.cores = cores;
    }

    public String getLimitCores() {
        return limitCores;
    }

    public void setLimitCores(String limitCores) {
        this.limitCores = limitCores;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }
}
