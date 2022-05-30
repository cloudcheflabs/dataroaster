package com.cloudcheflabs.dataroaster.operators.spark.handler;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.cloudcheflabs.dataroaster.operators.spark.api.dao.ResourceDao;
import com.cloudcheflabs.dataroaster.operators.spark.config.SparkConfiguration;
import com.cloudcheflabs.dataroaster.operators.spark.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.operators.spark.crd.*;
import com.cloudcheflabs.dataroaster.operators.spark.util.HttpUtils;
import com.cloudcheflabs.dataroaster.operators.spark.util.S3Utils;
import com.cloudcheflabs.dataroaster.operators.spark.util.SparkProcessExecutor;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SparkSubmitHandler {

    private static Logger LOG = LoggerFactory.getLogger(SparkSubmitHandler.class);

    public static String runApplication(SparkApplication sparkApplication) {
        try {
            // build spark configuration with spark application spec.

            Map<String, String> confMap = SparkConfiguration.defaultConf();

            SparkApplicationSpec spec = sparkApplication.getSpec();
            Core core = spec.getCore();

            Core.Container container = core.getContainer();
            String image = container.getImage();
            String imagePullPolicy = container.getImagePullPolicy();
            // set image and pull policy.
            confMap = SparkConfiguration.setContainerImageAndPullPolicy(confMap, image, imagePullPolicy);

            Core.S3 s3 = core.getS3();
            String bucket = s3.getBucket();
            // set file upload path.
            confMap = SparkConfiguration.setFileUploadPath(confMap, bucket);

            // set warehouse dir.
            confMap = SparkConfiguration.setWarehouseDir(confMap, bucket);

            // secret names for access key and secret key.
            String accessKeySecretName = s3.getAccessKey().getValueFrom().getSecretKeyRef().getName();
            String accessKeyKey = s3.getAccessKey().getValueFrom().getSecretKeyRef().getKey();

            String secretKeySecretName = s3.getSecretKey().getValueFrom().getSecretKeyRef().getName();
            String secretKeyKey = s3.getSecretKey().getValueFrom().getSecretKeyRef().getKey();

            // get access key and secret key from secret resource.
            ApplicationContext applicationContext = SpringContextSingleton.getInstance();
            ResourceDao resourceDao = applicationContext.getBean(ResourceDao.class);

            // get access key from secret map.
            Map<String, String> accessKeySecretMap = resourceDao.getSecret(sparkApplication.getMetadata().getNamespace(), accessKeySecretName);
            String accessKeyEncoded = accessKeySecretMap.get(accessKeyKey);
            String accessKeyDecoded = new String(Base64.getDecoder().decode(accessKeyEncoded));

            // get secret key from secret map.
            Map<String, String> secretKeySecretMap = resourceDao.getSecret(sparkApplication.getMetadata().getNamespace(), secretKeySecretName);
            String secretKeyEncoded = secretKeySecretMap.get(secretKeyKey);
            String secretKeyDecoded = new String(Base64.getDecoder().decode(secretKeyEncoded));

            String accessKey = accessKeyDecoded;
            String secretKey = secretKeyDecoded;
            String endpoint = s3.getEndpoint();

            // set s3 credentials.
            confMap = SparkConfiguration.setS3Credentials(confMap, bucket, accessKey, secretKey, endpoint);

            Core.Hive hive = core.getHive();
            if (hive != null) {
                List<String> metastoreUris = hive.getMetastoreUris();
                // set hive metastore uris.
                confMap = SparkConfiguration.setHiveMetastoreUris(confMap, metastoreUris);
            }

            // target namespace where spark application will be run.
            String namespace = core.getNamespace();

            Driver driver = spec.getDriver();
            String serviceAccount = driver.getServiceAccountName();

            // set namespace and service account.
            confMap = SparkConfiguration.setNamespaceAndServiceAccount(confMap, namespace, serviceAccount);

            Resources resources = driver.getResources();
            // set driver resources.
            confMap = SparkConfiguration.setDriverResources(confMap, resources.getCores(), resources.getLimitCores(), resources.getMemory());

            Map<String, String> driverLabelMap = driver.getLabel();
            if (driverLabelMap != null) {
                // set driver label.
                confMap = SparkConfiguration.setDriverLabel(confMap, driverLabelMap);
            }

            Map<String, String> driverAnnotationMap = driver.getAnnotation();
            if (driverAnnotationMap != null) {
                // set driver annotation.
                confMap = SparkConfiguration.setDriverAnnotation(confMap, driverAnnotationMap);
            }

            Executor executor = spec.getExecutor();
            int instances = executor.getInstances();
            Resources execResources = executor.getResources();
            // set executor resources.
            confMap = SparkConfiguration.setExecutorResources(confMap,
                    String.valueOf(instances),
                    execResources.getCores(),
                    execResources.getLimitCores(),
                    execResources.getMemory());

            Map<String, String> executorLabelMap = executor.getLabel();
            if (executorLabelMap != null) {
                // set executor label.
                confMap = SparkConfiguration.setExecutorLabel(confMap, executorLabelMap);
            }

            Map<String, String> executorAnnotationMap = executor.getAnnotation();
            if (executorAnnotationMap != null) {
                // set executor annotation.
                confMap = SparkConfiguration.setExecutorAnnotation(confMap, executorAnnotationMap);
            }

            Map<String, String> confs = spec.getConfs();
            if (confs != null) {
                // set extra spark configurations.
                confMap = SparkConfiguration.setConfs(confMap, confs);
            }

            // set volumes.
            List<Volume> volumes = spec.getVolumes();
            if (volumes != null) {
                List<VolumeMount> driverVolumeMounts = driver.getVolumeMounts();
                if (driverVolumeMounts != null) {
                    // set driver volumes.
                    confMap = SparkConfiguration.setDriverVolumes(confMap, volumes, driverVolumeMounts);
                }

                List<VolumeMount> executorVolumeMounts = executor.getVolumeMounts();
                if (executorVolumeMounts != null) {
                    // set executor volumes.
                    confMap = SparkConfiguration.setExecutorVolumes(confMap, volumes, executorVolumeMounts);
                }
            }

            // main class name.
            String mainClass = core.getClazz();

            // arguments of application.
            List<String> args = core.getArgs();
            String argsLine = null;
            if (args != null) {
                int argsSize = args.size();
                int count = 0;
                StringBuffer sb = new StringBuffer();
                for (String arg : args) {
                    sb.append(arg);
                    if (count != argsSize - 1) {
                        sb.append(" ");
                        count++;
                    } else {
                        break;
                    }
                }
                argsLine = sb.toString();
            }

            // download application file from s3 or rest server.
            String applicationFileUrl = core.getApplicationFileUrl();

            // application file name parsed from url.
            int index = applicationFileUrl.lastIndexOf("/");
            String fileName = applicationFileUrl.substring(index + 1, applicationFileUrl.length());

            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss");
            String formattedDate = fmt.print(DateTime.now());
            String uid = formattedDate + "-" + UUID.randomUUID().toString();

            // base temp directory for downloaded spark appliation files to be run.
            String sparkAppBaseTempDir = System.getProperty("java.io.tmpdir") + "/spark-app";

            // temp directory for the current spark application.
            String tempDirectory = sparkAppBaseTempDir + "/" + uid;

            // create temp. directory.
            FileUtils.createDirectory(tempDirectory);
            String toFilePath = tempDirectory + "/" + fileName;

            // set pod template.
            PodTemplate driverPodTemplate = driver.getPodTemplate();
            if (driverPodTemplate != null) {
                // set driver pod template.
                confMap = SparkConfiguration.createPodTemplate(confMap, driverPodTemplate, true, tempDirectory);
            }

            PodTemplate executorPodTemplate = executor.getPodTemplate();
            if (executorPodTemplate != null) {
                // set executor pod template.
                confMap = SparkConfiguration.createPodTemplate(confMap, driverPodTemplate, false, tempDirectory);
            }


            LOG.info("downloading application file...");

            // download application file from s3.
            if (applicationFileUrl.startsWith("s3a")) {
                S3Utils.downloadObject(accessKey,
                        secretKey,
                        endpoint,
                        applicationFileUrl,
                        toFilePath);
            }
            // download application file from http rest.
            else if (applicationFileUrl.startsWith("http")) {
                HttpUtils.downloadFileFromHttpRest(applicationFileUrl, toFilePath);
            }
            LOG.info("application file downloaded to [{}]: ", toFilePath);


            // python dependency zip, egg, py file.
            String pyFilesUrl = core.getPyFilesUrl();
            String pyFilesFilePath = null;

            if(pyFilesUrl != null) {
                int pyFilesIndex = pyFilesUrl.lastIndexOf("/");
                String pyDepsName = pyFilesUrl.substring(pyFilesIndex + 1, pyFilesUrl.length());
                pyFilesFilePath = tempDirectory + "/" + pyDepsName;

                // download application file from s3.
                if (pyFilesUrl.startsWith("s3a")) {
                    S3Utils.downloadObject(accessKey,
                            secretKey,
                            endpoint,
                            pyFilesUrl,
                            pyFilesFilePath);
                }
                // download application file from http rest.
                else if (pyFilesUrl.startsWith("http")) {
                    HttpUtils.downloadFileFromHttpRest(pyFilesUrl, pyFilesFilePath);
                }
                LOG.info("python deps file downloaded to [{}]: ", pyFilesFilePath);
            }

            // spark submit mode with the value of Cluster or Client.
            String deployMode = core.getDeployMode();

            // make run script.
            StringBuffer runScriptSb = new StringBuffer();
            runScriptSb.append("#!/bin/bash").append("\n").append("\n");
            runScriptSb.append("set -eux;").append("\n");
            runScriptSb.append("spark-submit").append(" \\").append("\n");
            runScriptSb.append("--master ").append(SparkConfiguration.DEFAULT_MASTER).append(" \\").append("\n");
            runScriptSb.append("--deploy-mode ").append(deployMode.toLowerCase()).append(" \\").append("\n");
            runScriptSb.append("--name ").append(sparkApplication.getMetadata().getName()).append(" \\").append("\n");
            if (mainClass != null) runScriptSb.append("--class ").append(mainClass).append(" \\").append("\n");
            runScriptSb.append("--packages ").append(SparkConfiguration.DEFAULT_PACKAGES).append(" \\").append("\n");
            for (String confKey : confMap.keySet()) {
                runScriptSb.append("--conf ").append(confKey).append("=").append("\"").append(confMap.get(confKey)).append("\"").append(" \\").append("\n");
            }
            if(pyFilesUrl != null) runScriptSb.append("--py-files ").append(pyFilesFilePath).append(" \\").append("\n");
            runScriptSb.append("file://").append(toFilePath);
            if (argsLine != null) runScriptSb.append(" \\").append("\n").append(argsLine);

            String runScriptLine = runScriptSb.toString();
            LOG.info("runScriptLine: \n{}", runScriptLine);

            // save run script to temp directory.
            String runScriptName = "run-spark-app.sh";
            FileUtils.stringToFile(runScriptLine, tempDirectory + "/" + runScriptName, true);
            LOG.info("run script saved to [{}]", tempDirectory + "/" + runScriptName);

            LOG.info("ready to run spark application script...");
            SparkProcessExecutor sparkProcessExecutor = new SparkProcessExecutor();

            // watch the status of spark application pod.
            WatchSparkApplicationPodStatus t = new WatchSparkApplicationPodStatus(sparkApplication, sparkProcessExecutor);
            t.start();

            // run spark job.
            sparkProcessExecutor.doExec(tempDirectory + "/" + runScriptName);

            // delete temp directory where downloaded spark application files are located.
            FileUtils.deleteDirectory(tempDirectory);
            LOG.info("temp directory [{}] deleted.", tempDirectory);

            if (t != null) {
                t.setStopped(true);
                t = null;
                LOG.info("WatchSparkApplicationPodStatus thread set to null...");
            }

            return "spark application [" + sparkApplication.getMetadata().getName() + "] has been deployed...";
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
        }

        return null;
    }

    private static class WatchSparkApplicationPodStatus extends Thread {

        private SparkApplication sparkApplication;
        private SparkProcessExecutor sparkProcessExecutor;
        private boolean stopped = false;

        public WatchSparkApplicationPodStatus(SparkApplication sparkApplication,
                                              SparkProcessExecutor sparkProcessExecutor) {
            this.sparkApplication = sparkApplication;
            this.sparkProcessExecutor = sparkProcessExecutor;
        }

        public void setStopped(boolean stopped) {
            this.stopped = stopped;
        }


        @Override
        public void run() {
            // watch spark application driver is running.
            KubernetesClient client = SpringContextSingleton.getInstance().getBean(KubernetesClient.class);
            String namespace = sparkApplication.getSpec().getCore().getNamespace();
            int MAX = 600;
            int count = 0;
            while(!stopped) {
                PodList podList = client.pods().inNamespace(namespace).list();
                if (podList != null) {
                    for (Pod pod : podList.getItems()) {
                        String podName = pod.getMetadata().getName();
                        if (podName.startsWith(sparkApplication.getMetadata().getName())) {
                            String phase = pod.getStatus().getPhase();
                            LOG.info("pod: [{}], phase: [{}]", podName, phase);
                            if (phase.equals("Running")) {
                                Map<String, String> labels = pod.getMetadata().getLabels();
                                if (labels.containsKey("spark-role")) {
                                    String sparkRole = labels.get("spark-role");
                                    if (sparkRole.equals("driver") || sparkRole.equals("executor")) {
                                        LOG.info("spark application [{}] is running now...", podName);
                                        LOG.info("current spark submit process will be killed...");
                                        sparkProcessExecutor.destroy();
                                        LOG.info("watch job is finished...");
                                        stopped = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(1000);
                    count++;
                    if(count == MAX) {
                        LOG.warn("spark application [{}] not found in cluster", sparkApplication.getMetadata().getName());
                        stopped = true;
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
