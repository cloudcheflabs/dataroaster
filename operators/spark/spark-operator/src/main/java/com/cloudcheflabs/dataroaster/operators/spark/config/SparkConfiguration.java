package com.cloudcheflabs.dataroaster.operators.spark.config;

import com.cloudcheflabs.dataroaster.operators.spark.crd.PodTemplate;
import com.cloudcheflabs.dataroaster.operators.spark.crd.Volume;
import com.cloudcheflabs.dataroaster.operators.spark.crd.VolumeMount;
import com.cloudcheflabs.dataroaster.operators.spark.domain.Pod;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparkConfiguration {

    public static final String DEFAULT_MASTER = System.getProperty("masterUrlForLocalTest", "k8s://https://kubernetes.default.svc");
    public static final String DEFAULT_PACKAGES = "com.amazonaws:aws-java-sdk-s3:1.12.316,org.apache.hadoop:hadoop-aws:3.3.5";
    public static final String DEFAULT_SPARK_OPERATOR_NAMESPACE = "spark-operator";

    public static Map<String, String> defaultConf() {
        Map<String, String> confMap = new HashMap<>();

        // hive.
        confMap.put("spark.hadoop.hive.metastore.client.connect.retry.delay", "5");
        confMap.put("spark.hadoop.hive.metastore.client.socket.timeout", "1800");
        confMap.put("spark.hadoop.hive.server2.enable.doAs", "false");
        confMap.put("spark.hadoop.hive.server2.thrift.http.port", "10002");
        confMap.put("spark.hadoop.hive.server2.thrift.port", "10016");
        confMap.put("spark.hadoop.hive.server2.transport.mode", "binary");
        confMap.put("spark.hadoop.metastore.catalog.default", "spark");
        confMap.put("spark.hadoop.hive.execution.engine", "spark");

        // s3.
        confMap.put("spark.hadoop.fs.s3a.connection.ssl.enabled", "true");
        confMap.put("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        confMap.put("spark.hadoop.fs.s3a.fast.upload", "true");
        confMap.put("spark.hadoop.fs.s3a.path.style.access", "true");

        // driver.
        confMap.put("spark.driver.extraJavaOptions", "-Divy.cache.dir=/tmp -Divy.home=/tmp");
        confMap.put("spark.kubernetes.driver.master", "https://kubernetes.default.svc");

        return confMap;
    }

    public static Map<String, String> setFileUploadPath(Map<String, String> confMap, String bucket) {
        confMap.put("spark.kubernetes.file.upload.path", "s3a://" + bucket + "/spark-apps-upload");
        return confMap;
    }

    public static Map<String, String> setWarehouseDir(Map<String, String> confMap, String bucket) {
        confMap.put("spark.sql.warehouse.dir", "s3a://" + bucket + "/apps/spark/warehouse");
        return confMap;
    }

    public static Map<String, String> setS3Credentials(Map<String, String> confMap, String bucket, String accessKey, String secretKey, String endpoint, String region) {
        confMap.put("spark.hadoop.fs.defaultFS", "s3a://" + bucket);
        confMap.put("spark.hadoop.fs.s3a.access.key", accessKey);
        confMap.put("spark.hadoop.fs.s3a.secret.key", secretKey);
        confMap.put("spark.hadoop.fs.s3a.endpoint", endpoint);
        confMap.put("spark.hadoop.fs.s3a.endpoint.region", region);
        return confMap;
    }

    public static Map<String, String> setHiveMetastoreUris(Map<String, String> confMap, List<String> metastoreUris) {
        StringBuffer uriLine = new StringBuffer();
        int uriSize = metastoreUris.size();
        int count = 0;
        for(String metastoreUri : metastoreUris) {
            uriLine.append(metastoreUri);
            if(count != uriSize -1) {
                uriLine.append(",");
                count++;
            } else {
                break;
            }
        }
        confMap.put("spark.hadoop.hive.metastore.uris", uriLine.toString());
        return confMap;
    }

    public static Map<String, String> setNamespaceAndServiceAccount(Map<String, String> confMap, String namespace, String serviceAccount) {
        confMap.put("spark.kubernetes.namespace", namespace);
        confMap.put("spark.kubernetes.authenticate.driver.serviceAccountName", serviceAccount);
        return confMap;
    }

    public static Map<String, String> setContainerImageAndPullPolicy(Map<String, String> confMap, String image, String pullPolicy) {
        confMap.put("spark.kubernetes.container.image", image);
        confMap.put("spark.kubernetes.container.image.pullPolicy", pullPolicy);
        return confMap;
    }

    public static Map<String, String> setDriverResources(Map<String, String> confMap, String cores, String limitCores, String memory) {
        if(cores != null) confMap.put("spark.driver.cores", cores);
        if(limitCores != null) confMap.put("spark.kubernetes.driver.limit.cores", limitCores);
        if(memory != null) confMap.put("spark.driver.memory", memory);
        return confMap;
    }

    public static Map<String, String> setExecutorResources(Map<String, String> confMap, String instances, String cores, String limitCores, String memory) {
        confMap.put("spark.executor.instances", instances);
        if(cores != null) confMap.put("spark.executor.cores", cores);
        if(limitCores != null) confMap.put("spark.kubernetes.executor.limit.cores", limitCores);
        if(memory != null) confMap.put("spark.executor.memory", memory);
        return confMap;
    }

    public static Map<String, String> setDriverLabel(Map<String, String> confMap, Map<String, String> labelMap) {
        for(String key : labelMap.keySet()) {
            confMap.put("spark.kubernetes.driver.label." + key, labelMap.get(key));
        }
        return confMap;
    }

    public static Map<String, String> setExecutorLabel(Map<String, String> confMap, Map<String, String> labelMap) {
        for(String key : labelMap.keySet()) {
            confMap.put("spark.kubernetes.executor.label." + key, labelMap.get(key));
        }
        return confMap;
    }

    public static Map<String, String> setDriverAnnotation(Map<String, String> confMap, Map<String, String> annotationMap) {
        for(String key : annotationMap.keySet()) {
            confMap.put("spark.kubernetes.driver.annotation." + key, annotationMap.get(key));
        }
        return confMap;
    }

    public static Map<String, String> setExecutorAnnotation(Map<String, String> confMap, Map<String, String> annotationMap) {
        for(String key : annotationMap.keySet()) {
            confMap.put("spark.kubernetes.executor.annotation." + key, annotationMap.get(key));
        }
        return confMap;
    }

    public static Map<String, String> setConfs(Map<String, String> confMap, Map<String, String> confs) {
        for(String key : confs.keySet()) {
            confMap.put(key, confs.get(key));
        }
        return confMap;
    }

    public static Map<String, String> setDriverVolumes(Map<String, String> confMap, List<Volume> volumes, List<VolumeMount> volumeMounts) {
        Map<String, Volume> volumeMap = new HashMap<>();
        for(Volume volume : volumes) {
            volumeMap.put(volume.getName(), volume);
        }

        for(VolumeMount volumeMount : volumeMounts) {
            String volumeMountName = volumeMount.getName();
            Volume volume = volumeMap.get(volumeMountName);

            // type of spark local dir or any other general volume.
            String type = volume.getType();

            // volume type.
            String volumeType = null;
            if(volume.getEmptyDir() != null) volumeType = "emptyDir";
            else if(volume.getHostPath() != null) volumeType = "hostPath";
            else if(volume.getPersistentVolumeClaim() != null) volumeType = "persistentVolumeClaim";

            if(volumeType == null) continue;

            // type for spark local dir.
            if(type.equals("SparkLocalDir")) {
                confMap = addVolumeMountWithVolumeOptions(confMap, volumeMount, volume, volumeType, true, true);
            } else {
                confMap = addVolumeMountWithVolumeOptions(confMap, volumeMount, volume, volumeType, false, true);
            }
        }
        return confMap;
    }

    public static Map<String, String> setExecutorVolumes(Map<String, String> confMap, List<Volume> volumes, List<VolumeMount> volumeMounts) {
        Map<String, Volume> volumeMap = new HashMap<>();
        for(Volume volume : volumes) {
            volumeMap.put(volume.getName(), volume);
        }

        for(VolumeMount volumeMount : volumeMounts) {
            String volumeMountName = volumeMount.getName();
            Volume volume = volumeMap.get(volumeMountName);

            // type of spark local dir or any other general volume.
            String type = volume.getType();

            // volume type.
            String volumeType = null;
            if(volume.getEmptyDir() != null) volumeType = "emptyDir";
            else if(volume.getHostPath() != null) volumeType = "hostPath";
            else if(volume.getPersistentVolumeClaim() != null) volumeType = "persistentVolumeClaim";

            if(volumeType == null) continue;

            // type for spark local dir.
            if(type.equals("SparkLocalDir")) {
                confMap = addVolumeMountWithVolumeOptions(confMap, volumeMount, volume, volumeType, true, false);
            } else {
                confMap = addVolumeMountWithVolumeOptions(confMap, volumeMount, volume, volumeType, false, false);
            }
        }
        return confMap;
    }

    private static Map<String, String> addVolumeMountWithVolumeOptions(Map<String, String> confMap, VolumeMount volumeMount, Volume volume, String volumeType, boolean sparkLocalDir, boolean driver) {
        String volumeMountName = volumeMount.getName();
        String mountPath = volumeMount.getMountPath();
        String subPath = volumeMount.getSubPath();
        boolean readOnly = volumeMount.isReadOnly();

        String driverOrExecutor = (driver) ? "driver" : "executor";
        String volumeName = (sparkLocalDir) ? "spark-local-dir-" + volumeMountName : volumeMountName;

        // add volume mount.
        confMap.put("spark.kubernetes." + driverOrExecutor + ".volumes." + volumeType + "." + volumeName + ".mount.path", mountPath);
        confMap.put("spark.kubernetes." + driverOrExecutor + ".volumes." + volumeType + "." + volumeName + ".mount.readOnly", String.valueOf(readOnly));
        if(subPath != null) confMap.put("spark.kubernetes." + driverOrExecutor + ".volumes." + volumeType + "." + volumeName + ".mount.subPath", subPath);

        // add options of volume.
        if(volume.getEmptyDir() != null) {
            Volume.EmptyDir emptyDir = volume.getEmptyDir();
            String medium = emptyDir.getMedium();
            Object sizeLimit = emptyDir.getSizeLimit();
            if(medium != null) confMap.put("spark.kubernetes." + driverOrExecutor + ".volumes." + volumeType + "." + volumeName + ".options.medium", medium);
            if(sizeLimit != null) confMap.put("spark.kubernetes." + driverOrExecutor + ".volumes." + volumeType + "." + volumeName + ".options.sizeLimit", String.valueOf(sizeLimit));
        } else if(volume.getHostPath() != null) {
            Volume.HostPath hostPath = volume.getHostPath();
            String path = hostPath.getPath();
            String hostPathType = hostPath.getType();
            confMap.put("spark.kubernetes." + driverOrExecutor + ".volumes." + volumeType + "." + volumeName + ".options.path", path);
            if(hostPathType != null) confMap.put("spark.kubernetes." + driverOrExecutor + ".volumes." + volumeType + "." + volumeName + ".options.type", hostPathType);
        } else if(volume.getPersistentVolumeClaim() != null) {
            Volume.PersistentVolumeClaim persistentVolumeClaim = volume.getPersistentVolumeClaim();
            String claimName = persistentVolumeClaim.getClaimName();
            boolean pvcReadOnly = persistentVolumeClaim.isReadOnly();

            // TODO: support dynamic persistent volume allocation.

            /**
             * Example:
             * spark.kubernetes.executor.volumes.persistentVolumeClaim.data.options.claimName=OnDemand
             * spark.kubernetes.executor.volumes.persistentVolumeClaim.data.options.storageClass=gp
             * spark.kubernetes.executor.volumes.persistentVolumeClaim.data.options.sizeLimit=500Gi
             * spark.kubernetes.executor.volumes.persistentVolumeClaim.data.mount.path=/data
             * spark.kubernetes.executor.volumes.persistentVolumeClaim.data.mount.readOnly=false
             */

            //confMap.put("spark.kubernetes." + driverOrExecutor + ".volumes." + volumeType + "." + volumeName + ".options.claimName", claimName);
        }
        return confMap;
    }

    public static Map<String, String> createPodTemplate(Map<String, String> confMap, PodTemplate podTemplate, boolean driver, String tempDir) {
        String podTemplateFile = (driver) ? tempDir + "/driver-pod-template.yaml" : tempDir + "/executor-pod-template.yaml";
        // create pod template.
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        Pod pod = new Pod();
        pod.setSpec(podTemplate);

        // Write object as YAML file
        try {
            mapper.writeValue(new File(podTemplateFile), pod);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if(driver) {
            // creeate pod template file for driver.
            confMap.put("spark.kubernetes.driver.podTemplateFile", podTemplateFile);
        } else {
            // creeate pod template file for executor.
            confMap.put("spark.kubernetes.executor.podTemplateFile", podTemplateFile);
        }

        return confMap;
    }
}
