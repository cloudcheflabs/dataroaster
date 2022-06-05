package com.cloudcheflabs.dataroaster.operators.trino.crd;

public class Resources {
    private Requests requests;
    private Limits limits;

    public Requests getRequests() {
        return requests;
    }

    public void setRequests(Requests requests) {
        this.requests = requests;
    }

    public Limits getLimits() {
        return limits;
    }

    public void setLimits(Limits limits) {
        this.limits = limits;
    }

    public static class Requests {
        private String cpu;
        private String memory;
    }

    public static class Limits extends Requests {

    }
}
