# DataRoaster Spark Operator

DataRoaster Spark Operator is used to submit spark applications onto kubernetes easily. 
Not only spark batch job but also endless running spark applications like spark streaming aplications can be deployed using Custom Resource `SparkApplication` handled by dataroaster spark operator.
For instance, Spark Thrift Server which is one of the components provided by DataRoaster can be deployed onto kubernetes by this spark operator.

## Install DataRoaster Spark Operator

Install spark operator with helm.
```
helm repo add dataroaster-spark-operator https://cloudcheflabs.github.io/helm-repository/
helm repo update

helm install \
spark-operator \
--create-namespace \
--namespace spark-operator \
--version v1.2.0 \
--set image=cloudcheflabs/spark-operator:v3.4.0 \
dataroaster-spark-operator/dataroastersparkoperator;
```

Check if spark operator is running.
```
kubectl get po -n spark-operator
NAME                              READY   STATUS    RESTARTS   AGE
spark-operator-756fbf4479-s2zjt   1/1     Running   0          52s
```

## Custom Resource
Before creating custom resource `SparkApplication`, we need to understand Custom Resource Definition of it, 
see the [Custom Resource Definition](https://github.com/cloudcheflabs/dataroaster/blob/master/operators/spark/chart/templates/spark-applications.yaml) of `SparkApplication` in detail.

The overview of custom resource `SparkApplication` looks as follows.
```
apiVersion: "spark-operator.cloudchef-labs.com/v1alpha1"
kind: SparkApplication
metadata:
  name: spark-app-name
  namespace: spark-operator
spec:
  core:
    ...
  driver:
    ...
    podTemplate:
      ...
  executor:
    ...
    podTemplate:
      ...
  confs:
    ...
  volumes:
    ...
```

There are several elements inside element `spec`.
* `core`: define main class name, application file url, spark container image, etc.
* `driver`: define driver pod resources like cpu, memory, etc.
* `driver.podTemplate`: define driver pod template resources like affinity, tolerations, nodeSelector, etc.
* `executor`: define executor pod resources like cpu, memory, executor instance count, etc.
* `executor.podTemplate`: define executor pod template resources like affinity, tolerations, nodeSelector, etc.
* `confs`: define additional spark configurations.
* `volumes`: define volumes used by driver and executor pod.


For instance, let's see a custom resource to deploy spark thrift server.
```
apiVersion: "spark-operator.cloudchef-labs.com/v1alpha1"
kind: SparkApplication
metadata:
  name: spark-thrift-server-minimal
  namespace: spark-operator
spec:
  core:
    applicationType: EndlessRun
    deployMode: Cluster
    container:
      image: "cloudcheflabs/spark:v3.4.0"
      imagePullPolicy: Always
    class: com.cloudcheflabs.dataroaster.hive.SparkThriftServerRunner
    applicationFileUrl: "s3a://mykidong/spark-app/spark-thrift-server-4.8.0-SNAPSHOT-spark-job.jar"
    namespace: spark
    s3:
      bucket: mykidong
      accessKey:
        valueFrom:
          secretKeyRef:
            name: s3-secret
            key: accessKey
      secretKey:
        valueFrom:
          secretKeyRef:
            name: s3-secret
            key: secretKey
      endpoint: "https://any-s3-endpoint"
      region: "us-east-1"
    hive:
      metastoreUris:
        - "thrift://metastore.dataroaster-hivemetastore.svc:9083"
  driver:
    serviceAccountName: spark
    label:
      application-name: spark-thrift-server-driver
    resources:
      cores: "1"
      limitCores: "1200m"
      memory: "512m"
    volumeMounts:
      - name: driver-local-dir
        mountPath: "/tmp/local-dir"
  executor:
    instances: 1
    label:
      application-name: spark-thrift-server-executor
    resources:
      cores: "1"
      limitCores: "1200m"
      memory: "1g"
    volumeMounts:
      - name: executor-local-dir
        mountPath: "/tmp/local-dir"
  volumes:
    - name: driver-local-dir
      type: SparkLocalDir
      persistentVolumeClaim:
        claimName: OnDemand
        storageClass: nfs
        sizeLimit: 50Gi
    - name: executor-local-dir
      type: SparkLocalDir
      persistentVolumeClaim:
        claimName: OnDemand
        storageClass: nfs
        sizeLimit: 50Gi
```



## Publishment

* [Deploy spark thrift server to kubernetes using dataroaster spark operator](https://mykidong.medium.com/hive-on-spark-with-spark-operator-9a43ea7ebe06)

