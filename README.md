# DataRoaster
DataRoaster is open source data platform running on Kubernetes.

## DataRoaster Architecture

![DataRoaster Architecture](https://github.com/cloudcheflabs/dataroaster/blob/master/operators/dataroaster/dataroaster-operator/docs/images/dataroaster-architecture.jpg)

DataRoaster has a simple architecture. There are several operators in DataRoaster to install data platform components easily.
* `DataRoaster Operator` handles rest requests to create custom resources.
* After creating custom resources, `Helm Operator` and other operators will be notified and create data platform components like spark thrift server, trino cluster, etc.


## Install DataRoaster

### Install MySQL Server
MySQL server needs to be installed first.
```
helm repo add dataroaster-mysql https://cloudcheflabs.github.io/mysql-helm-repo/
helm repo update

helm install \
mysql \
--create-namespace \
--namespace dataroaster-operator \
--version v1.0.1 \
--set storage.storageClass=oci \
dataroaster-mysql/dataroaster-mysql;
```

`storage.storageClass` needs to be replaced with storage class of your kubernetes cluster.


To create db schema, download db schema file of [create-tables.sql](https://github.com/cloudcheflabs/dataroaster/tree/master/operators/dataroaster/dataroaster-operator/sql/create-tables.sql), and run the following.


```
kubectl exec -it mysql-statefulset-0 -n dataroaster-operator -- mysql -u root -pmysqlpass123 < ./create-tables.sql;
```

### Install DataRoaster Operator

```
helm repo add dataroaster-operator https://cloudcheflabs.github.io/dataroaster-operator-helm-repo/
helm repo update;

helm install \
dataroaster-operator \
--create-namespace \
--namespace dataroaster-operator \
--version v1.0.0 \
dataroaster-operator/dataroaster-operator;
```

Let's list pods in dataroaster namespace.

```
kubectl get po -n dataroaster-operator;
NAME                                   READY   STATUS    RESTARTS   AGE
dataroaster-operator-5d9c6cbf9-n5nww   1/1     Running   0          12m
helm-operator-5464cdc75f-z96ld         1/1     Running   0          12m
mysql-statefulset-0                    1/1     Running   0          8h
```


You need to copy the randomly generated admin password which will be used to access dataroaster api.

```
kubectl logs -f dataroaster-operator-5d9c6cbf9-n5nww -n dataroaster-operator;
...
randomly generated password for user 'admin': 9a87f65688a64e999e62c8c308509708
...
```
`9a87f65688a64e999e62c8c308509708` is the temporary admin password which can be used for the first time and should be changed in the future.


To access DataRoaster from local, port-forward the service of `dataroaster-operator-service`.

```
kubectl port-forward svc/dataroaster-operator-service 8089 -n dataroaster-operator;
```


## REST API

### Login

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


### Users

#### Create
Parameters:
* `user`: Username.
* `password`: Password.

```
# get token.
curl -XPOST \
http://localhost:8089/v1/login \
-d  "user=admin" \
-d "password=9a87f65688a64e999e62c8c308509708" > auth.json;
export ACCESS_TOKEN="$(jq -r '.token' auth.json)";

# request.
curl -XPOST -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/users/create \
-d  "user=user1" \
-d "password=password1";
```


#### Update Password
Parameters:
* `user`: Username.
* `password`: Password.

```
# get token
curl -XPOST \
http://localhost:8089/v1/login \
-d  "user=admin" \
-d "password=9a87f65688a64e999e62c8c308509708" > auth.json;
export ACCESS_TOKEN="$(jq -r '.token' auth.json)";

# request.
curl -XPUT -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/users/update/password \
-d  "user=admin" \
-d "password=adminpass";
```


#### List
Parameters: NONE

```
# get token.
curl -XPOST \
http://localhost:8089/v1/login \
-d  "user=admin" \
-d "password=adminpass" > auth.json;
export ACCESS_TOKEN="$(jq -r '.token' auth.json)";

# request.
curl -XGET -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/users/list ;
```



#### Delete
Parameters:
* `user`: Username.

```
# get token.
curl -XPOST \
http://localhost:8089/v1/login \
-d  "user=admin" \
-d "password=adminpass" > auth.json;
export ACCESS_TOKEN="$(jq -r '.token' auth.json)"; 

# request.
curl -XDELETE -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/users/delete \
-d "user=user1";
```


### Custom Resource

#### Create
Parameters:
* `yaml`:  Base64 encoded string of custom resource yaml file.


For instance, create trino cluster.
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
EOF

   
```


```
# get token.
curl -XPOST \
http://localhost:8089/v1/login \
-d  "user=admin" \
-d "password=adminpass" > auth.json;
export ACCESS_TOKEN="$(jq -r '.token' auth.json)";

# request.
curl -XPOST -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/cr/create \
-d  "yaml=$(base64 -w 0 ./trino-cluster-etl.yaml)";
```



#### Update
Parameters:
* `yaml`:  Base64 encoded string of custom resource yaml file.


```
# get token.
curl -XPOST \
http://localhost:8089/v1/login \
-d  "user=admin" \
-d "password=adminpass" > auth.json;
export ACCESS_TOKEN="$(jq -r '.token' auth.json)";

# request.
curl -XPUT -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/cr/update \
-d  "yaml=$(base64 -w 0 ./any-custom-resource.yaml)";
```


#### List
Parameters: NONE

```
# get token.
curl -XPOST \
http://localhost:8089/v1/login \
-d  "user=admin" \
-d "password=adminpass" > auth.json;
export ACCESS_TOKEN="$(jq -r '.token' auth.json)";

# request.
curl -XGET -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/cr/list ;
```


#### Delete
Parameters:
* `name`:  Custom resource name.
* `namespace`:  Namespace where custom resource is located.
* `kind`:  Kind of custom resource.


```
# get token.
curl -XPOST \
http://localhost:8089/v1/login \
-d  "user=admin" \
-d "password=adminpass" > auth.json;
export ACCESS_TOKEN="$(jq -r '.token' auth.json)";

# request.
curl -XDELETE -H "Authorization: Bearer $ACCESS_TOKEN" \
http://localhost:8089/v1/cr/delete \
-d  "name=trino-cluster-etl" \
-d  "namespace=trino-operator" \
-d  "kind=TrinoCluster";
```


## DataRoaster Spark Operator
DataRoaster Spark Operator is used to submit and delete spark applications on kubernetes using custom resources easily. Not only spark batch job but also endless running applications like spark streaming applications can be deployed using dataroaster spark operator.

See [DataRoaster Spark Operator](https://github.com/cloudcheflabs/dataroaster/tree/master/operators/spark) for more details.


## DataRoaster Trino Operator
DataRoaster Trino Operator is used to create/delete trino clusters easily.

See [DataRoaster Trino Operator](https://github.com/cloudcheflabs/dataroaster/tree/master/operators/trino) for more details.

## DataRoaster Trino Gateway
DataRoaster Trino Gateway is used to route the trino queries dynamically to downstream trino clusters.

See [DataRoaster Trino Gateway](https://github.com/cloudcheflabs/dataroaster/tree/master/trino-ecosystem/trino-gateway) for more details.


## DataRoaster Helm Operator
DataRoaster Helm Operator is used to install / upgrade / uninstall applications of helm charts easily.

See [DataRoaster Helm Operator](https://github.com/cloudcheflabs/dataroaster/tree/master/operators/helm) for more details.


## Community

* DataRoaster Community Mailing Lists: https://groups.google.com/g/dataroaster



## License
The use and distribution terms for this software are covered by the Apache 2.0 license.
