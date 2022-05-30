#!/bin/sh

## define namespace
NAMESPACE=dataroaster-spark-thrift-server;

# move to temp dir.
export TEMP_DIR=/tmp/spark-temp
mkdir -p ${TEMP_DIR};
cd ${TEMP_DIR};

# install spark.
export SPARK_VERSION=3.0.3
curl -L -O https://github.com/cloudcheflabs/spark/releases/download/v${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-custom-spark.tgz;

tar zxvf spark-${SPARK_VERSION}-bin-custom-spark.tgz;
mv spark-${SPARK_VERSION}-bin-custom-spark spark;
rm -rf spark-${SPARK_VERSION}*.tgz;

# download spark thrift server jar.
export SPARK_THRIFT_SERVER_FILE_NAME=spark-thrift-server-${SPARK_VERSION}-spark-job;
curl -L -O https://github.com/cloudcheflabs/spark/releases/download/v${SPARK_VERSION}/${SPARK_THRIFT_SERVER_FILE_NAME}.jar;

# set spark home.
SPARK_HOME=${TEMP_DIR}/spark;
PATH=$PATH:$SPARK_HOME/bin;


cd ${TEMP_DIR};

echo "whoami: $(whoami), java: $JAVA_HOME"

# export kubeconfig.
export KUBECONFIG=/home/opc/.kube/config;

echo "KUBECONFIG: $KUBECONFIG";

# create rbac.
kubectl create namespace ${NAMESPACE};
kubectl create serviceaccount spark -n ${NAMESPACE};
kubectl create clusterrolebinding ${NAMESPACE}-spark-role --clusterrole=edit --serviceaccount=${NAMESPACE}:spark;


# create spark pvc.
cat <<EOF > spark-pvc.yml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: spark-driver-pvc
  namespace: ${NAMESPACE}
  labels: {}
  annotations: {}
spec:
  accessModes:
  - ReadWriteMany
  resources:
    requests:
      storage: 2Gi
  storageClassName: nfs
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: spark-exec-pvc
  namespace: ${NAMESPACE}
  labels: {}
  annotations: {}
spec:
  accessModes:
  - ReadWriteMany
  resources:
    requests:
      storage: 50Gi
  storageClassName: nfs
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: spark-driver-localdir-pvc
  namespace: ${NAMESPACE}
  labels: {}
  annotations: {}
spec:
  accessModes:
  - ReadWriteMany
  resources:
    requests:
      storage: 2Gi
  storageClassName: nfs
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: spark-exec-localdir-pvc
  namespace: ${NAMESPACE}
  labels: {}
  annotations: {}
spec:
  accessModes:
  - ReadWriteMany
  resources:
    requests:
      storage: 50Gi
  storageClassName: nfs
EOF

kubectl apply -f spark-pvc.yml;



# submit spark thrift server job.
SPARK_IMAGE=cloudcheflabs/spark:v${SPARK_VERSION};
SPARK_MASTER=k8s://https://146.56.182.160:6443
S3_BUCKET=mykidong;
S3_ACCESS_KEY=TOW32G9ULH63MTUI6NNW;
S3_SECRET_KEY=jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi;
S3_ENDPOINT=https://ceph-rgw-test.cloudchef-labs.com;
HIVE_METASTORE=metastore.dataroaster-hivemetastore.svc:9083;

spark-submit \
--master $SPARK_MASTER \
--deploy-mode cluster \
--name spark-thrift-server \
--class com.cloudcheflabs.dataroaster.hive.SparkThriftServerRunner \
--packages com.amazonaws:aws-java-sdk-s3:1.11.375,org.apache.hadoop:hadoop-aws:3.2.0 \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.checkpointpvc.mount.path=/checkpoint \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.checkpointpvc.mount.subPath=checkpoint \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.checkpointpvc.mount.readOnly=false \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.checkpointpvc.options.claimName=spark-driver-pvc \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.checkpointpvc.mount.path=/checkpoint \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.checkpointpvc.mount.subPath=checkpoint \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.checkpointpvc.mount.readOnly=false \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.checkpointpvc.options.claimName=spark-exec-pvc \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.mount.path=/localdir \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.mount.readOnly=false \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.options.claimName=spark-driver-localdir-pvc \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.mount.path=/localdir \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.mount.readOnly=false \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.options.claimName=spark-exec-localdir-pvc \
--conf spark.kubernetes.file.upload.path=s3a://${S3_BUCKET}/spark-thrift-server \
--conf spark.kubernetes.container.image.pullPolicy=Always \
--conf spark.kubernetes.namespace=$NAMESPACE \
--conf spark.kubernetes.container.image=${SPARK_IMAGE} \
--conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
--conf spark.hadoop.hive.metastore.client.connect.retry.delay=5 \
--conf spark.hadoop.hive.metastore.client.socket.timeout=1800 \
--conf spark.hadoop.hive.metastore.uris=thrift://${HIVE_METASTORE} \
--conf spark.hadoop.hive.server2.enable.doAs=false \
--conf spark.hadoop.hive.server2.thrift.http.port=10002 \
--conf spark.hadoop.hive.server2.thrift.port=10016 \
--conf spark.hadoop.hive.server2.transport.mode=binary \
--conf spark.hadoop.metastore.catalog.default=spark \
--conf spark.hadoop.hive.execution.engine=spark \
--conf spark.sql.warehouse.dir=s3a://${S3_BUCKET}/apps/spark/warehouse \
--conf spark.hadoop.fs.defaultFS=s3a://${S3_BUCKET} \
--conf spark.hadoop.fs.s3a.access.key=${S3_ACCESS_KEY} \
--conf spark.hadoop.fs.s3a.secret.key=${S3_SECRET_KEY} \
--conf spark.hadoop.fs.s3a.connection.ssl.enabled=true \
--conf spark.hadoop.fs.s3a.endpoint=$S3_ENDPOINT \
--conf spark.hadoop.fs.s3a.impl=org.apache.hadoop.fs.s3a.S3AFileSystem \
--conf spark.hadoop.fs.s3a.fast.upload=true \
--conf spark.hadoop.fs.s3a.path.style.access=true \
--conf spark.driver.extraJavaOptions="-Divy.cache.dir=/tmp -Divy.home=/tmp" \
--conf spark.executor.instances=2 \
--conf spark.executor.memory=2G \
--conf spark.executor.cores=1 \
--conf spark.driver.memory=1G \
file://${TEMP_DIR}/${SPARK_THRIFT_SERVER_FILE_NAME}.jar \
> /dev/null 2>&1 &

PID=$!
echo "$PID" > pid;

echo "pid created...";

# check if spark thrift server pod is running.

# wait for a while.
echo "wait for 20s to initialize driver...";
sleep 20
echo "wait for spark executor being ready..."
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=spark-role=driver \
  --timeout=600s

echo "wait for 100s to initialize executors...";
sleep 100
echo "wait for spark executor being ready..."
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=spark-role=executor \
  --timeout=600s


# kill current spark submit process.
kill $(cat pid);

# create service.
kubectl apply -f /home/opc/dataroaster/charts/query-engine/1.0.0/spark-thrift-server/spark-thrift-server-service.yaml;

unset KUBECONFIG;
unset SPARK_HOME;

cd /tmp;
rm -rf ${TEMP_DIR};






