package com.cloudcheflabs.dataroaster.operators.spark.handler;

import com.cloudcheflabs.dataroaster.operators.spark.crd.SparkApplication;

public interface ActionHandler {
    void submit(SparkApplication sparkApplication);
    void destroy(SparkApplication sparkApplication);
}
