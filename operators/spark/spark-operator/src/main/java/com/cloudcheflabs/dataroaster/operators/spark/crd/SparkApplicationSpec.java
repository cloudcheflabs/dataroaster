package com.cloudcheflabs.dataroaster.operators.spark.crd;

import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.List;
import java.util.Map;


@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class SparkApplicationSpec implements KubernetesResource {

    private Core core;
    private Driver driver;
    private Executor executor;
    private Map<String, String> confs;
    private List<Volume> volumes;

    public List<Volume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<Volume> volumes) {
        this.volumes = volumes;
    }

    public Map<String, String> getConfs() {
        return confs;
    }

    public void setConfs(Map<String, String> confs) {
        this.confs = confs;
    }

    public Core getCore() {
        return core;
    }

    public void setCore(Core core) {
        this.core = core;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }


    @Override
    public String toString() {
        return JsonWriter.formatJson(JsonUtils.toJson(new ObjectMapper(), this));
    }
}
