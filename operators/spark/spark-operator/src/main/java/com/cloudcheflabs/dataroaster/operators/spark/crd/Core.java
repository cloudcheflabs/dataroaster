package com.cloudcheflabs.dataroaster.operators.spark.crd;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class Core {

    private String applicationType;
    private String deployMode;
    private Container container;
    @JsonProperty("class")
    private String clazz;
    private List<String> args;
    private String applicationFileUrl;
    private String pyFilesUrl;
    private String namespace;
    private S3 s3;
    private Hive hive;

    public String getPyFilesUrl() {
        return pyFilesUrl;
    }

    public void setPyFilesUrl(String pyFilesUrl) {
        this.pyFilesUrl = pyFilesUrl;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getApplicationFileUrl() {
        return applicationFileUrl;
    }

    public void setApplicationFileUrl(String applicationFileUrl) {
        this.applicationFileUrl = applicationFileUrl;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public S3 getS3() {
        return s3;
    }

    public void setS3(S3 s3) {
        this.s3 = s3;
    }

    public Hive getHive() {
        return hive;
    }

    public void setHive(Hive hive) {
        this.hive = hive;
    }

    public static class Hive {
        private List<String> metastoreUris;

        public List<String> getMetastoreUris() {
            return metastoreUris;
        }

        public void setMetastoreUris(List<String> metastoreUris) {
            this.metastoreUris = metastoreUris;
        }
    }



    public static class S3 {
        private String bucket;
        private AccessKey accessKey;
        private SecretKey secretKey;
        private String endpoint;
        private String region;

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public AccessKey getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(AccessKey accessKey) {
            this.accessKey = accessKey;
        }

        public SecretKey getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(SecretKey secretKey) {
            this.secretKey = secretKey;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public static class AccessKey {
            private ValueFrom valueFrom;

            public ValueFrom getValueFrom() {
                return valueFrom;
            }

            public void setValueFrom(ValueFrom valueFrom) {
                this.valueFrom = valueFrom;
            }
        }

        public static class SecretKey {
            private ValueFrom valueFrom;

            public ValueFrom getValueFrom() {
                return valueFrom;
            }

            public void setValueFrom(ValueFrom valueFrom) {
                this.valueFrom = valueFrom;
            }
        }
    }



    public static class Container {
        private String image;
        private String imagePullPolicy;

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getImagePullPolicy() {
            return imagePullPolicy;
        }

        public void setImagePullPolicy(String imagePullPolicy) {
            this.imagePullPolicy = imagePullPolicy;
        }
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(new ObjectMapper(), this);
    }
}
