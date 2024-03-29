package com.cloudcheflabs.dataroaster.trino.gateway.domain;

import java.io.Serializable;

public class TrinoResponse implements Serializable {
    private String id;
    private String nextUri;

    private String infoUri;

    private String partialCancelUri;

    public String getPartialCancelUri() {
        return partialCancelUri;
    }

    public void setPartialCancelUri(String partialCancelUri) {
        this.partialCancelUri = partialCancelUri;
    }

    public String getInfoUri() {
        return infoUri;
    }

    public void setInfoUri(String infoUri) {
        this.infoUri = infoUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNextUri() {
        return nextUri;
    }

    public void setNextUri(String nextUri) {
        this.nextUri = nextUri;
    }
}
