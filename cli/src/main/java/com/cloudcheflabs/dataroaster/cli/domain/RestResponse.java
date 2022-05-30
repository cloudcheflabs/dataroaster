package com.cloudcheflabs.dataroaster.cli.domain;

import java.io.Serializable;


public class RestResponse implements Serializable {

    public static final int STATUS_OK = 200;

    private int statusCode;

    private String errorMessage;

    private String successMessage;


    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }
}
