# DataRoaster Trino Gateway

DataRoaster Trino Gateway is used to route trino queries dynamically to downstream trino clusters.

## Trino Gateway Architecture

![Trino Gateway Architecture](https://github.com/cloudcheflabs/dataroaster/blob/master/trino-ecosystem/trino-gateway/docs/images/trino-gateway.jpg)

* `Admin` creates and deletes trino clusters using trino operator like [DataRoaster Trino Operator](https://github.com/cloudcheflabs/dataroaster/tree/master/operators/trino).
* After creating trino clusters on kubernetes, `Admin` registers trino cluster and users to `Trino Gateway` to route trino queries to the registered trino clusters.
* `Admin` can deactivate trino clusters to which the queries will not be routed.
* When the queries are sent by clients, `Trino Gateway` first will authenticate users and find the cluster group to which the user belongs to, and the queries will be routed to the random selected downstream trino cluster which belongs to the cluster group.

## Install DataRoaster Trino Gateway


### Install NGINX Ingress Controller

```
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

## create namespace.
kubectl create namespace ingress-nginx;

helm install \
--namespace ingress-nginx \
ingress-nginx \
ingress-nginx/ingress-nginx \
--version 4.0.17;
```

### Install Cert Manager

```
helm repo add jetstack https://charts.jetstack.io
helm repo update

helm install cert-manager \
jetstack/cert-manager \
--namespace cert-manager \
--create-namespace \
--version v1.5.3 \
--set installCRDs=true;
```

Because we are going to use let's encrypt certificates to support TLS, create issuer for it.

```
## for production.
cat <<EOF > prod-issuer.yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: mykidong@gmail.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
EOF

kubectl apply -f prod-issuer.yaml;
```



### Install MySQL Server

```
helm repo add dataroaster-trino-gateway-mysql https://cloudcheflabs.github.io/mysql-helm-repo/
helm repo update


helm install \
mysql \
--create-namespace \
--namespace trino-gateway \
--version v1.0.0 \
--set storage.storageClass=oci \
dataroaster-trino-gateway-mysql/dataroaster-trino-gateway-mysql;
```
The storage class `storage.storageClass` needs to be changed.

To create db schema for trino gateway, download db schema file of [create-tables.sql](https://github.com/cloudcheflabs/dataroaster/tree/master/trino-ecosystem/trino-gateway/sql/create-tables.sql), and run this.

```
kubectl exec -it mysql-statefulset-0 -n trino-gateway -- mysql -u root -pmysqlpass123 < ./create-tables.sql;
```


### Add ingress host names to public dns server
You can add the following entries to dns.
```
trino-gateway-proxy-test.cloudchef-labs.com ---> 146.56.150.205
trino-gateway-rest-test.cloudchef-labs.com  ---> 146.56.150.205
```
, where `146.56.150.205` is external ip of nginx service.

These host names will be used to create ingresses of trino gateway later.

### Install Trino Gateway

```
helm repo add dataroaster-trino-gateway https://cloudcheflabs.github.io/trino-gateway-helm-repo/
helm repo update

helm install \
trino-gateway \
--create-namespace \
--namespace trino-gateway \
--version v1.0.3 \
--set ingress.proxyHostName=trino-gateway-proxy-test.cloudchef-labs.com \
--set ingress.restHostName=trino-gateway-rest-test.cloudchef-labs.com \
dataroaster-trino-gateway/dataroaster-trino-gateway;
```
`ingress.proxyHostName` and `ingress.restHostName` need to be replaced with the registered host names to dns above.
 
Now, make sure certificates have been created successfully.
```
kubectl get certificate -n trino-gateway;
NAME                                              READY   SECRET                                            AGE
trino-gateway-proxy-test.cloudchef-labs.com-tls   True    trino-gateway-proxy-test.cloudchef-labs.com-tls   46s
trino-gateway-rest-test.cloudchef-labs.com-tls    True    trino-gateway-rest-test.cloudchef-labs.com-tls    46s
```

## Install Trino Clusters using Trino Operator

### Install Trino Operator

```
helm repo add dataroaster-trino-operator https://cloudcheflabs.github.io/trino-helm-repo/
helm repo update

helm install \
trino-operator \
--create-namespace \
--namespace trino-operator \
--version v1.0.0 \
dataroaster-trino-operator/dataroaster-trino-operator;
```

### Create trino clustes with custom resource

Create custom resource of trino cluster, for instance `cluster-1.yaml`.
```
apiVersion: "trino-operator.cloudchef-labs.com/v1beta1"
kind: TrinoCluster
metadata:
  name: trino-cluster-etl         # (1)
  namespace: trino-operator
spec:
  namespace: trino-cluster-etl    # (2)
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
          discovery.uri=http://trino-coordinator-service.trino-cluster-etl.svc:8080   # (3)
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
To create custom resource, run like this.
```
kubectl apply -f cluster-1.yaml;
```

To create second and third trino cluster, `# (1)`, `# (2)`, `# (3)` need to be modified and saved as another yaml files, namely, `cluster-2.yaml` and `cluster-3.yaml`.
For instance, 
* `# (1)` is custom resource name created in the namespace of trino operator. `trino-cluster-etl-2` and `trino-cluster-etl-3` for 2. cluster and 3. cluster respectly.
* `# (2)` is namespace where trino cluster will be created. `trino-cluster-etl-2` and `trino-cluster-etl-3` for 2. cluster and 3. cluster respectly.
* `# (3)` is discovery url to which trino worker will be registered. `http://trino-coordinator-service.trino-cluster-etl-2.svc:8080` and `http://trino-coordinator-service.trino-cluster-etl-3.svc:8080` for 2. cluster and 3. cluster respectly.


Create additional two trino clusters.
```
kubectl apply -f cluster-2.yaml;
kubectl apply -f cluster-3.yaml;
```

Now three trino clusters have been created.


## Create Ingresses for trino cluster coordinator services

### Add ingress host names to public dns server

```
trino-cluster-etl-test.cloudchef-labs.com ---> 146.56.150.205
trino-cluster-etl-2-test.cloudchef-labs.com ---> 146.56.150.205
trino-cluster-etl-3-test.cloudchef-labs.com ---> 146.56.150.205
```

These host names will be used when trino cluster ingresses are created.

### Create Trino Cluster Ingresses

We are going to create three ingresses for the trino cluster coordinator services created before.


```
cat <<EOF > trino-cluster-etl-ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/ssl-redirect: "false"
  name: trino-cluster-etl-ingress
  namespace: trino-cluster-etl
spec:
  ingressClassName: nginx
  rules:
  - host: trino-cluster-etl-test.cloudchef-labs.com
    http:
      paths:
      - backend:
          service:
            name: trino-coordinator-service
            port:
              number: 8080
        path: /
        pathType: ImplementationSpecific
  tls:
  - hosts:
    - trino-cluster-etl-test.cloudchef-labs.com
    secretName: trino-cluster-etl-test.cloudchef-labs.com-tls
EOF

cat <<EOF > trino-cluster-etl-2-ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/ssl-redirect: "false"
  name: trino-cluster-etl-ingress
  namespace: trino-cluster-etl-2
spec:
  ingressClassName: nginx
  rules:
  - host: trino-cluster-etl-2-test.cloudchef-labs.com
    http:
      paths:
      - backend:
          service:
            name: trino-coordinator-service
            port:
              number: 8080
        path: /
        pathType: ImplementationSpecific
  tls:
  - hosts:
    - trino-cluster-etl-2-test.cloudchef-labs.com
    secretName: trino-cluster-etl-2-test.cloudchef-labs.com-tls
EOF


cat <<EOF > trino-cluster-etl-3-ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/ssl-redirect: "false"
  name: trino-cluster-etl-ingress
  namespace: trino-cluster-etl-3
spec:
  ingressClassName: nginx
  rules:
  - host: trino-cluster-etl-3-test.cloudchef-labs.com
    http:
      paths:
      - backend:
          service:
            name: trino-coordinator-service
            port:
              number: 8080
        path: /
        pathType: ImplementationSpecific
  tls:
  - hosts:
    - trino-cluster-etl-3-test.cloudchef-labs.com
    secretName: trino-cluster-etl-3-test.cloudchef-labs.com-tls
EOF

kubectl apply -f trino-cluster-etl-ingress.yaml;
kubectl apply -f trino-cluster-etl-2-ingress.yaml;
kubectl apply -f trino-cluster-etl-3-ingress.yaml;
```

## Register trino clusters to trino gateway

### Create cluster group

```
curl -XPOST \
https://trino-gateway-rest-test.cloudchef-labs.com/v1/cluster_group/create \
-d  "group_name=etl_group";
```

### Register users

```
## register user who belongs to the cluster group.
curl -XPOST \
https://trino-gateway-rest-test.cloudchef-labs.com/v1/users/create \
-d  "user=trino" \
-d  "password=trino123" \
-d  "group_name=etl_group";
```

### Register trino clusters

```
# register cluster 1.
curl -XPOST \
https://trino-gateway-rest-test.cloudchef-labs.com/v1/cluster/create \
-d  "cluster_name=etl-trino-cluster-1" \
-d  "cluster_type=etl" \
-d  "url=https://trino-cluster-etl-test.cloudchef-labs.com" \
-d  "activated=true" \
-d  "group_name=etl_group";

# register cluster 2.
curl -XPOST \
https://trino-gateway-rest-test.cloudchef-labs.com/v1/cluster/create \
-d  "cluster_name=etl-trino-cluster-2" \
-d  "cluster_type=etl" \
-d  "url=https://trino-cluster-etl-2-test.cloudchef-labs.com" \
-d  "activated=true" \
-d  "group_name=etl_group";


# register cluster 3.
curl -XPOST \
https://trino-gateway-rest-test.cloudchef-labs.com/v1/cluster/create \
-d  "cluster_name=etl-trino-cluster-3" \
-d  "cluster_type=etl" \
-d  "url=https://trino-cluster-etl-3-test.cloudchef-labs.com" \
-d  "activated=true" \
-d  "group_name=etl_group";
-d  "activated=true" \
-d  "group_name=etl_group";
```

## Run queries with client

Trino client will be used to run queries to trino gateway.

### Install trino client.

```
mkdir -p ~/trino-cli;
cd ~/trino-cli;

curl -L -O https://repo1.maven.org/maven2/io/trino/trino-cli/384/trino-cli-384-executable.jar;
mv trino-cli-384-executable.jar trino
chmod +x trino

./trino --version;
```

### Connect to trino gateway

```
# user/password: trino/trino123
./trino --server https://trino-gateway-proxy-test.cloudchef-labs.com \
--user trino \
--password;
```

and run queries.

```
...
trino> SELECT count(*) FROM tpch.tiny.nation;
...
```

## Trino Gateway REST API


### Cluster Group
#### Create
Parameters:
* `group_name` : Unique cluster group name, for instance `etl_group`, `interactive_group`, `scheduled_group` .

```
curl -XPOST \
http://localhost:8099/v1/cluster_group/create \
-d  "group_name=etl_group";
```

#### List
Parameters: NO

```
curl -XGET \
http://localhost:8099/v1/cluster_group/list;
```


#### Delete
Parameters:
* `group_name` : Cluster group name.

```
curl -XDELETE \
http://localhost:8099/v1/cluster_group/delete \
-d  "group_name=etl_group";
```


### Cluster
#### Create
Parameters:
* `cluster_name` : Unique cluster name.
* `cluster_type` : Trino cluster type, for instance `etl`, `interactive`, `scheduled` .
* `url` : Backend Trino URL
* `activated` :  Status of trino cluster. `true` or `false`.
* `group_name` : Cluster group name to which the cluster belongs.

```
curl -XPOST \
http://localhost:8099/v1/cluster/create \
-d  "cluster_name=etl-trino-cluster-1" \
-d  "cluster_type=etl" \
-d  "url=http://localhost:8080" \
-d  "activated=true" \
-d  "group_name=etl_group";
```


#### Update Activated
Parameters:
* `cluster_name` : Cluster name.
* `activated` :  Status of trino cluster. `true` or `false`.

```
curl -XPUT \
http://localhost:8099/v1/cluster/update/activated \
-d  "cluster_name=etl-trino-cluster-1" \
-d  "activated=false";
```


#### List
Parameters: NO

```
curl -XGET \
http://localhost:8099/v1/cluster/list;
```


#### Delete
Parameters:
* `cluster_name` : Cluster name.

```
curl -XDELETE \
http://localhost:8099/v1/cluster/delete \
-d  "cluster_name=etl-trino-cluster-1";
```




### Users
#### Create
Parameters:
* `user` : Unique user name.
* `password` : Password.
* `group_name` : Cluster group name to which the user belongs.

```
curl -XPOST \
http://localhost:8099/v1/users/create \
-d  "user=trino" \
-d  "password=trino123" \
-d  "group_name=etl_group";
```


#### Update Password
Parameters:
* `user` : User name.
* `password` : Updated password.

```
curl -XPUT \
http://localhost:8099/v1/users/update/password \
-d  "user=trino" \
-d  "password=trino123updated";
```


#### List
Parameters: NO

```
curl -XGET \
http://localhost:8099/v1/users/list;
```


#### Delete
Parameters:
* `user` : User name.

```
curl -XDELETE \
http://localhost:8099/v1/users/delete \
-d  "user=trino";
```
 

 
