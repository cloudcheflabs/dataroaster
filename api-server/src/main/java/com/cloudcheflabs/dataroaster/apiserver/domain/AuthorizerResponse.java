package com.cloudcheflabs.dataroaster.apiserver.domain;

import java.io.Serializable;

/**
 * Created by mykidong on 2019-08-28.
 */
public class AuthorizerResponse implements Serializable {

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
