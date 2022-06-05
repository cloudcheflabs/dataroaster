package com.cloudcheflabs.dataroaster.operators.trino.crd;

public class Toleration {
    private String effect;
    private String key;
    private String operator;
    private int tolerationSeconds;
    private String value;

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public int getTolerationSeconds() {
        return tolerationSeconds;
    }

    public void setTolerationSeconds(int tolerationSeconds) {
        this.tolerationSeconds = tolerationSeconds;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
