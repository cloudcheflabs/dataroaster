# DataRoaster Trino Operator

DataRoaster Trino Operator is used to create and delete trino clusters easily using custom resources.

## Install DataRoaster Trino Operator with Helm

Add trino operator helm repository.
```
helm repo add dataroaster-trino-operator https://cloudcheflabs.github.io/trino-helm-repo/
helm repo update
```

Install trino operator with helm.
```
helm install \
trino-operator \
--create-namespace \
--namespace trino-operator \
--version v2.0.1 \
dataroaster-trino-operator/dataroaster-trino-operator;
```

Check if trino operator is running.
```
kubectl get po -n trino-operator
NAME                             READY   STATUS    RESTARTS   AGE
trino-operator-89b86f46c-2sh8m   1/1     Running   0          6s
```

## Example: Create Trino Cluster with Custom Resource
Let's create trino cluster using example custom resource.

Example custom resource of trino cluster looks like this.
```
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
```

Clone dataroaster sources.
```
git clone https://github.com/cloudcheflabs/dataroaster.git;
cd dataroaster/operators/trino/trino-operator;
```

Run the following commands to create a trino cluster.
```
kubectl apply -f src/test/resources/cr/trino-cluster-etl.yaml;
```

And now trino operator will create trino coordinator and workers in the namespace `trino-cluster-etl` like this.
```
kubectl get all -n trino-cluster-etl
NAME                                     READY   STATUS    RESTARTS   AGE
pod/trino-coordinator-5b565cb97b-l8kv4   1/1     Running   0          11s
pod/trino-worker-7cd68f7fd-dqs52         1/1     Running   0          10s
pod/trino-worker-7cd68f7fd-z588f         1/1     Running   0          10s

NAME                                TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
service/trino-coordinator-service   ClusterIP   10.111.87.228   <none>        8080/TCP   10s

NAME                                READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/trino-coordinator   1/1     1            1           11s
deployment.apps/trino-worker        2/2     2            2           10s

NAME                                           DESIRED   CURRENT   READY   AGE
replicaset.apps/trino-coordinator-5b565cb97b   1         1         1       11s
replicaset.apps/trino-worker-7cd68f7fd         2         2         2       10s

NAME                                               REFERENCE                 TARGETS         MINPODS   MAXPODS   REPLICAS   AGE
horizontalpodautoscaler.autoscaling/trino-worker   Deployment/trino-worker   <unknown>/50%   2         5         0          9s
```

Let's run queries to trino coordinator using trino cli.
First, install trino cll.
```
mkdir -p ~/trino-cli;
cd ~/trino-cli;

curl -L -O https://repo1.maven.org/maven2/io/trino/trino-cli/384/trino-cli-384-executable.jar;
mv trino-cli-384-executable.jar trino
chmod +x trino

## version.
./trino --version;
```

Port-forward trino service to access trino coordinator.
```
# check trino services.
kubectl get svc -n trino-cluster-etl
NAME                        TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
trino-coordinator-service   ClusterIP   10.111.87.228   <none>        8080/TCP   3m27s

# port forwarding trino service.
kubectl port-forward svc/trino-coordinator-service 8080 -n trino-cluster-etl;
...
```

Run queries to trino using trino cli.
```
cd ~/trino-cli;

# connect trino coordinator.
./trino --server http://localhost:8080;
...
trino> SELECT count(*) FROM tpch.tiny.nation;
 _col0
-------
    25
(1 row)

Query 20220607_101434_00004_4nitv, FINISHED, 2 nodes
Splits: 10 total, 10 done (100.00%)
4.07 [25 rows, 0B] [6 rows/s, 0B/s]
...
```

Destroy trino cluster.
```
cd <dataroaster>/operators/trino/trino-operator;
kubectl delete -f src/test/resources/cr/trino-cluster-etl.yaml;
```

Uninstall trino operator.
```
helm uninstall trino-operator -n trino-operator;
```