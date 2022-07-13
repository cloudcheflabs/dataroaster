# DataRoaster Trino Operator

DataRoaster Trino Operator is used to create and delete trino clusters easily using custom resources.

## Install DataRoaster Trino Operator with Helm

```
helm repo add dataroaster-trino-operator https://cloudcheflabs.github.io/trino-helm-repo/
helm repo update

helm install \
trino-operator \
--create-namespace \
--namespace trino-operator \
--version v2.1.6 \
dataroaster-trino-operator/dataroastertrinooperator;
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

## Example: Create Trino Cluster with the support of Trino Monitoring using JMX and Prometheus JMX Exporter

There is [another example](https://github.com/cloudcheflabs/dataroaster/blob/master/operators/trino/trino-operator/src/test/resources/cr/trino-cluster-etl-jmx.yaml) 
to create trino cluster with the support of trino monitoring using jmx and prometheus jmx exporter.
After installing this trino cluster, several headless services will be created to expose jmx metrics from trino coordinator and workers.

Let's see the endpoints of these services.

```
NAME                                    ENDPOINTS                           AGE
trino-coordinator-jmxexporter-service   10.244.0.15:9090                    161m
trino-coordinator-rmi-service           10.244.0.15:9081                    161m
trino-coordinator-rmiregistry-service   10.244.0.15:9080                    161m
trino-worker-jmxexporter-service        10.244.0.16:9090,10.244.1.10:9090   161m
trino-worker-rmi-service                10.244.0.16:9081,10.244.1.10:9081   161m
trino-worker-rmiregistry-service        10.244.0.16:9080,10.244.1.10:9080   161m
```

To access mbeans exposed by coordinator pod, the endpoint of `trino-coordinator-rmiregistry-service` will be used, 
and for worker pods, the endpoints of `trino-worker-rmiregistry-service` will be used for the jmx clients to get mbeans exposed by coordinator and workers.


There are the endpoints of prometheus jmx exporter to integrate jmx metrics with prometheus. The endpoints of `trino-coordinator-jmxexporter-service` for coordinator and 
`trino-worker-jmxexporter-service` for workers will be used by prometheus. With adding these endpoints addresses to scrape configs in prometheus configuration, 
all the jmx metrics exposed by prometheus jmx exporter embedded in coordinator and workers will be collected to prometheus.

To demonstrate the prometheus integration, install prometheus like this.

```
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update


helm install \
prometheus \
--create-namespace \
--namespace prometheus \
--version 15.10.2 \
--set alertmanager.persistentVolume.storageClass=oci \
--set server.persistentVolume.storageClass=oci \
--set pushgateway.persistentVolume.storageClass=oci \
prometheus-community/prometheus;
```
All the `*.storageClass` need to be changed to suit your kubernetees cluster, 
and configure prometheus configmap with adding the jmx exporter endpoints to scrape configs.

```
kubectl edit cm prometheus-server -n prometheus;
...
	scrape_configs:
    - job_name: trino-coordinator
      static_configs:
      - targets:
        - trino-coordinator-jmxexporter-service.trino-cluster-etl.svc:9090
    - job_name: trino-worker-0
      static_configs:
      - targets:
        - 10.244.0.16:9090
    - job_name: trino-worker-1
      static_configs:
      - targets:
        - 10.244.1.10:9090
...
```




## JMX REST API

This JMX REST API is used to access mbeans exposed by trino coordinator and workers.

To test apis, port-forward trino operator service.
```
kubectl port-forward svc/trino-operator-service 8092 -n trino-operator;
```


### List Trino Clusters
Parameters:
* `namespace`: namespace where trino cluster custom resources exists.


```
curl -G \
http://localhost:8092/v1/cluster/list_clusters \
-d "namespace=trino-operator" \
;
```



### List MBeans
Parameters:
* `namespace`: namespace where trino cluster custom resources exists.
* `cluster_name`: trino custer name.


```
curl -G \
http://localhost:8092/v1/jmx/list_mbeans \
-d "namespace=trino-operator" \
-d "cluster_name=trino-cluster-etl" \
;
```


### Get MBean Value
Parameters:
* `namespace`: namespace where trino cluster custom resources exists.
* `cluster_name`: trino custer name.
* `object_name`: mbean object name.
* `attribute`: mbean attribute.
* `composite_key`: composite key if attribute value is the type of composite data.



```
curl -G \
http://localhost:8092/v1/jmx/get_value \
--data-urlencode "namespace=trino-operator" \
--data-urlencode "cluster_name=trino-cluster-etl" \
--data-urlencode "object_name=trino.execution:name=QueryExecution" \
--data-urlencode "attribute=Executor.MaximumPoolSize" \
;
```

```
curl -G \
http://localhost:8092/v1/jmx/get_value \
--data-urlencode "namespace=trino-operator" \
--data-urlencode "cluster_name=trino-cluster-etl" \
--data-urlencode "object_name=java.lang:type=Memory" \
--data-urlencode "attribute=HeapMemoryUsage" \
--data-urlencode "composite_key=committed" \
;
```


## Scale Worker REST API

This is used to scale workers.

### List Worker Count

Parameters:
* `namespace`: namespace where trino cluster custom resources exists.


```
curl -G \
http://localhost:8092/v1/scale/list_worker_count \
--data-urlencode "namespace=trino-operator" \
;
```

### Scale out Workers
Parameters:
* `namespace`: namespace where trino cluster custom resources exists.
* `cluster_name`: trino custer name.
* `replicas`: replica count of workers in this trino cluster.


```
curl -XPUT \
http://localhost:8092/v1/scale/scale_workers \
--data-urlencode "namespace=trino-operator" \
--data-urlencode "cluster_name=trino-cluster-etl" \
--data-urlencode "replicas=4" \
;
```


### List HPA

Parameters:
* `namespace`: namespace where trino cluster custom resources exists.


```
curl -G \
http://localhost:8092/v1/scale/list_hpa \
--data-urlencode "namespace=trino-operator" \
;
```


### Update HPA
Parameters:
* `namespace`: namespace where trino cluster custom resources exists.
* `cluster_name`: trino custer name.
* `min_replicas`: min. replicas of workers.
* `max_replicas`: max. replicas of workers.


```
curl -XPUT \
http://localhost:8092/v1/scale/scale_hpa \
--data-urlencode "namespace=trino-operator" \
--data-urlencode "cluster_name=trino-cluster-etl" \
--data-urlencode "min_replicas=3" \
--data-urlencode "max_replicas=6" \
;
```



