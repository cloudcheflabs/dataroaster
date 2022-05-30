package com.cloudcheflabs.dataroaster.apiserver.util;

import org.springframework.http.HttpStatus;

/**
 * Created by mykidong on 2019-08-29.
 */
public class HttpUtils {

    public static HttpStatus getHttpStatus(int statusCode)
    {
        HttpStatus currentHttpStatus = null;
        for(HttpStatus httpStatus : HttpStatus.values())
        {
            if(httpStatus.value() == statusCode)
            {
                currentHttpStatus = httpStatus;

                break;
            }
        }

        currentHttpStatus = (currentHttpStatus == null) ? HttpStatus.NOT_FOUND : currentHttpStatus;

        return currentHttpStatus;
    }
}
