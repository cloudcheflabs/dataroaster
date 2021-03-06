# DataRoaster REST API

## Login

Parameters:
* `user`: Username.
* `password`: Password.

```
# request.
curl -XPOST \
http://localhost:8089/v1/login \
-d  "user=admin" \
-d "password=9a87f65688a64e999e62c8c308509708";

# response.
...
{"expiration":"2022-06-22T15:22:09.196Z","token":"JDJhJDA4JFJtcXpJUld2d04wcGsxRC93QXBRcXVZdTB3amlyVWVsdDhsa2lWOXdGTW5GM09KYzBxRnhx"}
```

`token` needs to be embedded in request header of `Authorization` at every request.


## Users

### Create
Parameters:
* `user`: Username.
* `password`: Password.

```
curl -XPOST -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/users/create \
-d  "user=user1" \
-d "password=password1";
```


### Update Password
Parameters:
* `user`: Username.
* `password`: Password.

```
curl -XPUT -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/users/update/password \
-d  "user=admin" \
-d "password=adminpass";
```


### List
Parameters: NONE

```
curl -XGET -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/users/list ;
```



### Delete
Parameters:
* `user`: Username.

```
curl -XDELETE -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/users/delete \
-d "user=user1";
```


## Hive Metastore

### Create
Parameters:
* `mysql_storage_class`: storage class for mysql.
* `mysql_storage_size`: mysql storage size in Gi.
* `yaml`:  base64 encoded string of hive metastore custom resource yaml file.


```
cat <<EOF > hive-metastore.yaml
apiVersion: "helm-operator.cloudchef-labs.com/v1beta1"
kind: HelmChart
metadata:
  name: hive-metastore
  namespace: dataroaster-operator
spec:
  repo: https://cloudcheflabs.github.io/hive-metastore-helm-repo/
  chartName: dataroaster-hivemetastore
  name: hive-metastore
  version: v2.0.0
  namespace: hive-metastore
  values: |
    image: cloudcheflabs/hivemetastore:v3.0.0
    s3:
      bucket: any-bucket
      accessKey: any-access-key
      secretKey: any-secret-key
      endpoint: https://any-endpoint
EOF
   
```


```
curl -XPOST -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/hive_metastore/create \
-d  "mysql_storage_class=standard" \
-d  "mysql_storage_size=5" \
-d  "yaml=$(base64 -w 0 ./hive-metastore.yaml)";
```



### List
Parameters: NONE

```
curl -XGET -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/hive_metastore/list ;
```


### Delete
Parameters: NONE

```
curl -XDELETE -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/hive_metastore/delete ;
```


## Spark Thrift Server

### Create
Parameters:
* `nfs_storage_class`: storage class for nfs.
* `nfs_storage_size`: nfs storage size in Gi.
* `s3_access_key`: base64 encoded string of s3 access key.
* `s3_secret_key`: base64 encoded string of s3 secret key.
* `pvc_size`: spark application pvc size in Gi.
* `yaml`:  base64 encoded string of spark thrift server custom resource yaml file.


```
cat <<EOF > spark-thrift-server.yaml
apiVersion: "spark-operator.cloudchef-labs.com/v1alpha1"
kind: SparkApplication
metadata:
  name: spark-thrift-server
  namespace: spark-operator
spec:
  core:
    applicationType: EndlessRun
    deployMode: Cluster
    container:
      image: "cloudcheflabs/spark:v3.0.3"
      imagePullPolicy: Always
    class: com.cloudcheflabs.dataroaster.hive.SparkThriftServerRunner
    applicationFileUrl: "s3a://mykidong/spark-app/spark-thrift-server-3.0.3-spark-job.jar"
    namespace: spark-thrift-server
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
      endpoint: "https://any-endpoint"
    hive:
      metastoreUris:
        - "thrift://metastore.hive-metastore.svc:9083"
  driver:
    serviceAccountName: spark-thrift-server
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
        claimName: nfs-pvc-driver
    - name: executor-local-dir
      type: SparkLocalDir
      persistentVolumeClaim:
        claimName: nfs-pvc-executor
EOF 
```


```
curl -XPOST -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/spark_thrift_server/create \
-d  "nfs_storage_class=standard" \
-d  "nfs_storage_size=10" \
-d  "s3_access_key=$(echo -n 'any-access-key' | base64)" \
-d  "s3_secret_key=$(echo -n 'any-secret-key' | base64)" \
-d  "pvc_size=1" \
-d  "yaml=$(base64 -w 0 ./spark-thrift-server.yaml)";
```



### List
Parameters: NONE

```
curl -XGET -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/spark_thrift_server/list ;
```


### Delete
Parameters: NONE

```
curl -XDELETE -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/spark_thrift_server/delete ;
```



## Trino

### Create
Parameters:
* `yaml`:  base64 encoded string of trino custom resource yaml file.


```
cat <<EOF > trino-cluster-etl.yaml
apiVersion: "trino-operator.cloudchef-labs.com/v1beta1"
kind: TrinoCluster
metadata:
  name: trino-cluster-etl
  namespace: trino-operator
spec:
  namespace: trino-cluster-etl
  serviceAccountName: trino
  image:
    repository: trinodb/trino
    tag: "384"
    imagePullPolicy: IfNotPresent
    imagePullSecrets: null
  securityContext:
    runAsUser: 1000
    runAsGroup: 1000
  coordinator:
    resources: null
    nodeSelector: null
    affinity: null
    tolerations: null
    configs:
      - name: node.properties
        path: /etc/trino
        value: |
          node.environment=production
          node.data-dir=/data/trino
          plugin.dir=/usr/lib/trino/plugin
      - name: config.properties
        path: /etc/trino
        value: |
          coordinator=true
          node-scheduler.include-coordinator=false
          http-server.http.port=8080
          query.max-memory=4GB
          query.max-memory-per-node=1GB
          memory.heap-headroom-per-node=1GB
          discovery-server.enabled=true
          discovery.uri=http://localhost:8080
      - name: jvm.config
        path: /etc/trino
        value: |
          -server
          -Xmx8G
          -XX:+UseG1GC
          -XX:G1HeapRegionSize=32M
          -XX:+UseGCOverheadLimit
          -XX:+ExplicitGCInvokesConcurrent
          -XX:+HeapDumpOnOutOfMemoryError
          -XX:+ExitOnOutOfMemoryError
          -Djdk.attach.allowAttachSelf=true
          -XX:-UseBiasedLocking
          -XX:ReservedCodeCacheSize=512M
          -XX:PerMethodRecompilationCutoff=10000
          -XX:PerBytecodeRecompilationCutoff=10000
          -Djdk.nio.maxCachedBufferSize=2000000
          -XX:+UnlockDiagnosticVMOptions
          -XX:+UseAESCTRIntrinsics
      - name: log.properties
        path: /etc/trino
        value: |
          io.trino=INFO
      - name: tpch.properties
        path: /etc/trino/catalog
        value: |
          connector.name=tpch
          tpch.splits-per-node=4
      - name: tpcds.properties
        path: /etc/trino/catalog
        value: |
          connector.name=tpcds
          tpcds.splits-per-node=4
      - name: hive.properties
        path: /etc/trino/catalog
        value: |
          connector.name=hive-hadoop2
          hive.metastore.uri=thrift://metastore.hive-metastore.svc:9083
          hive.allow-drop-table=true
          hive.max-partitions-per-scan=1000000
          hive.compression-codec=NONE
          hive.s3.path-style-access=true
          hive.s3.ssl.enabled=true
          hive.s3.max-connections=100
          hive.s3.endpoint=https://any-endpoint
          hive.s3.aws-access-key=any-access-key
          hive.s3.aws-secret-key=any-secret-key
  worker:
    replicas: 2
    autoscaler:
      minReplicas: 2
      maxReplicas: 5
      targetCPUUtilizationPercentage: 50
    resources: null
    nodeSelector: null
    affinity: null
    tolerations: null
    configs:
      - name: node.properties
        path: /etc/trino
        value: |
          node.environment=production
          node.data-dir=/data/trino
          plugin.dir=/usr/lib/trino/plugin
      - name: config.properties
        path: /etc/trino
        value: |
          coordinator=false
          http-server.http.port=8080
          query.max-memory=4GB
          query.max-memory-per-node=1GB
          memory.heap-headroom-per-node=1GB
          discovery.uri=http://trino-coordinator-service.trino-cluster-etl.svc:8080
      - name: jvm.config
        path: /etc/trino
        value: |
          -server
          -Xmx8G
          -XX:+UseG1GC
          -XX:G1HeapRegionSize=32M
          -XX:+UseGCOverheadLimit
          -XX:+ExplicitGCInvokesConcurrent
          -XX:+HeapDumpOnOutOfMemoryError
          -XX:+ExitOnOutOfMemoryError
          -Djdk.attach.allowAttachSelf=true
          -XX:-UseBiasedLocking
          -XX:ReservedCodeCacheSize=512M
          -XX:PerMethodRecompilationCutoff=10000
          -XX:PerBytecodeRecompilationCutoff=10000
          -Djdk.nio.maxCachedBufferSize=2000000
          -XX:+UnlockDiagnosticVMOptions
          -XX:+UseAESCTRIntrinsics
      - name: log.properties
        path: /etc/trino
        value: |
          io.trino=INFO
      - name: tpch.properties
        path: /etc/trino/catalog
        value: |
          connector.name=tpch
          tpch.splits-per-node=4
      - name: tpcds.properties
        path: /etc/trino/catalog
        value: |
          connector.name=tpcds
          tpcds.splits-per-node=4
      - name: hive.properties
        path: /etc/trino/catalog
        value: |
          connector.name=hive-hadoop2
          hive.metastore.uri=thrift://metastore.hive-metastore.svc:9083
          hive.allow-drop-table=true
          hive.max-partitions-per-scan=1000000
          hive.compression-codec=NONE
          hive.s3.path-style-access=true
          hive.s3.ssl.enabled=true
          hive.s3.max-connections=100
          hive.s3.endpoint=https://any-endpoint
          hive.s3.aws-access-key=any-access-key
          hive.s3.aws-secret-key=any-secret-key
EOF
```


```
curl -XPOST -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/trino/create \
-d  "yaml=$(base64 -w 0 ./trino-cluster-etl.yaml)";
```



### List
Parameters: NONE

```
curl -XGET -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/trino/list ;
```


### Delete
Parameters: NONE

```
curl -XDELETE -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/trino/delete ;
```




## Redash

### Create
Parameters:
* `yaml`:  base64 encoded string of redash custom resource yaml file.


```
cat <<EOF > redash.yaml
apiVersion: "helm-operator.cloudchef-labs.com/v1beta1"
kind: HelmChart
metadata:
  name: redash
  namespace: dataroaster-operator
spec:
  repo: https://cloudcheflabs.github.io/redash-helm-repo/
  chartName: dataroasterredash
  name: redash
  version: v2.0.0
  namespace: redash
  values: |
    storage:
      storageClass: oci
      size: 2
    service:
      type: LoadBalancer
EOF
```


```
curl -XPOST -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/redash/create \
-d  "yaml=$(base64 -w 0 ./redash.yaml)";
```



### List
Parameters: NONE

```
curl -XGET -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/redash/list ;
```


### Delete
Parameters: NONE

```
curl -XDELETE -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/redash/delete ;
```



## Jupyterhub

### Create
Parameters:
* `yaml`:  base64 encoded string of jupyterhub custom resource yaml file.


```
cat <<EOF > jupyterhub.yaml
apiVersion: "helm-operator.cloudchef-labs.com/v1beta1"
kind: HelmChart
metadata:
  name: jupyterhub
  namespace: dataroaster-operator
spec:
  repo: https://charts.bitnami.com/bitnami
  chartName: jupyterhub
  name: jupyterhub
  version: 1.3.6
  namespace: jupyterhub
  values: |
    hub:
      service:
        type: LoadBalancer
    singleuser:
      image:
        registry: docker.io
        repository: cloudcheflabs/dataroaster-bitnami-jupyter
        tag: 1.5.0
        pullPolicy: Always
      persistence:
        storageClass: oci
        size: 15Gi
EOF
```


```
curl -XPOST -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/jupyterhub/create \
-d  "yaml=$(base64 -w 0 ./jupyterhub.yaml)";
```



### List
Parameters: NONE

```
curl -XGET -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/jupyterhub/list ;
```


### Delete
Parameters: NONE

```
curl -XDELETE -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/jupyterhub/delete ;
```



## Kafka

### Create
Parameters:
* `yaml`:  base64 encoded string of kafka custom resource yaml file.


```
cat <<EOF > kafka.yaml
apiVersion: "helm-operator.cloudchef-labs.com/v1beta1"
kind: HelmChart
metadata:
  name: kafka
  namespace: dataroaster-operator
spec:
  repo: https://charts.bitnami.com/bitnami
  chartName: kafka
  name: kafka
  version: 18.0.0
  namespace: kafka
  values: |
    replicaCount: 3
    persistence:
      enabled: true
      size: 8Gi
      storageClass: oci
    zookeeper:
      replicaCount: 3
      persistence:
        enabled: true
        storageClass: oci
EOF
```


```
curl -XPOST -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/kafka/create \
-d  "yaml=$(base64 -w 0 ./kafka.yaml)";
```



### List
Parameters: NONE

```
curl -XGET -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/kafka/list ;
```


### Delete
Parameters: NONE

```
curl -XDELETE -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/kafka/delete ;
```



## Airflow

### Create
Parameters:
* `nfs_storage_class`: storage class for nfs.
* `nfs_storage_size`: nfs storage size in Gi.
* `yaml`:  base64 encoded string of spark thrift server custom resource yaml file.


```
cat <<EOF > airflow.yaml
apiVersion: "helm-operator.cloudchef-labs.com/v1beta1"
kind: HelmChart
metadata:
  name: airflow
  namespace: dataroaster-operator
spec:
  repo: https://airflow.apache.org
  chartName: airflow
  name: airflow
  version: 1.4.0
  namespace: airflow
  values: |
    defaultAirflowRepository: cloudcheflabs/airflow
    defaultAirflowTag: "2.2.3-u01"
    webserver:
      service:
        type: LoadBalancer
    flower:
      service:
        type: LoadBalancer
    ingress:
      enabled: false
    workers:
      replicas: 3
      persistence:
        enabled: true
        size: 3Gi
        storageClassName: oci
    redis:
      enabled: true
      persistence:
        enabled: true
        size: 1Gi
        storageClassName: oci
    ports:
      flowerUI: 5555
      airflowUI: 8080
      workerLogs: 8793
      redisDB: 6379
      statsdIngest: 9125
      statsdScrape: 9102
      pgbouncer: 6543
      pgbouncerScrape: 9127
    postgresql:
      enabled: true
      persistence:
        storageClass: oci
    config:
      core:
        remote_logging: 'True'
      api:
        auth_backend: airflow.api.auth.backend.deny_all
      logging:
        remote_logging: 'True'
      celery:
        worker_concurrency: 16
    extraEnv: |
      - name: AIRFLOW_CONN_OCI_S3
        value: "s3://@?host=<endpoint>&aws_access_key_id=<access-key>&aws_secret_access_key=<secret-key>"
      - name: AIRFLOW__CORE__REMOTE_LOGGING
        value: 'True'
      - name: AIRFLOW__CORE__REMOTE_BASE_LOG_FOLDER
        value: s3://mykidong/airflow/logs
      - name: AIRFLOW__CORE_REMOTE_LOG_CONN_ID
        value: oci_s3
      - name: AIRFLOW__CORE__ENCRYPT_S3_LOGS
        value: 'False'
      - name: AIRFLOW__LOGGING__REMOTE_LOGGING
        value: 'True'
      - name: AIRFLOW__LOGGING__REMOTE_BASE_LOG_FOLDER
        value: s3://mykidong/airflow/logs
      - name: AIRFLOW__LOGGING__REMOTE_LOG_CONN_ID
        value: oci_s3
      - name: AIRFLOW__LOGGING__ENCRYPT_S3_LOGS
        value: 'False'
    dags:
      persistence:
        enabled: true
        size: 1Gi
        storageClassName: nfs
        accessMode: ReadWriteMany
      gitSync:
        enabled: false
    logs:
      persistence:
        enabled: true
        size: 2Gi
        storageClassName: nfs
EOF
```


```
curl -XPOST -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/airflow/create \
-d  "nfs_storage_class=oci" \
-d  "nfs_storage_size=10" \
-d  "yaml=$(base64 -w 0 ./airflow.yaml)";
```



### List
Parameters: NONE

```
curl -XGET -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/airflow/list ;
```


### Delete
Parameters: NONE

```
curl -XDELETE -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/airflow/delete ;
```
