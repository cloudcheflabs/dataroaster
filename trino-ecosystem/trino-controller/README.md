# DataRoaster Trino Controller

DataRoaster Trino Controller is used to control all the trino ecosystem components like 
[DataRoaster Trino Gateway](https://github.com/cloudcheflabs/dataroaster/tree/master/trino-ecosystem/trino-gateway) and
[DataRoaster Trino Operator](https://github.com/cloudcheflabs/dataroaster/tree/master/operators/trino) to build trino gateway easily.

## Trino Gateway Architecture with Controller

![Trino Gateway Architecture with Controller](https://github.com/cloudcheflabs/dataroaster/blob/master/trino-ecosystem/trino-controller/docs/images/trino-gateway-architecture-with-controller.jpg)


* `Admin` sends requests to `Trino Controller` to create and delete trino clusters, and then`Trino Controller` will let [DataRoaster Trino Operator](https://github.com/cloudcheflabs/dataroaster/tree/master/operators/trino) create and delete trino clusters.
* When trino clusters created, the addresses of prometheus jmx exporter embedded in trino coordinator and workers will be added as jobs to prometheus configmap in `Prometheus` to collect jmx metrics exposed by trino coordinator and workers.
* `Admin` will send requests to `Trino Controller` to register trino cluster and users to `Trino Gateway` to route trino queries to the registered trino clusters.
* `Admin` can send request to `Trino Controller` to deactivate trino clusters to which the queries will not be routed.
* When the queries are sent by clients, `Trino Gateway` first will authenticate users and find the cluster group to which the user belongs to, and the queries will be routed to the random selected downstream trino cluster which belongs to the cluster group.

## Install DataRoaster Trino Controller

### Install Trino Controller
```
helm repo add dataroaster-trino-controller https://cloudcheflabs.github.io/trino-controller-helm-repo/
helm repo update

helm install \
trino-controller \
--create-namespace \
--namespace trino-controller \
--version v1.3.0 \
--set trino.gateway.publicEndpoint="https://trino-gateway-proxy-test.cloudchef-labs.com" \
--set trino.gateway.proxyHostName=trino-gateway-proxy-test.cloudchef-labs.com \
--set trino.gateway.restHostName=trino-gateway-rest-test.cloudchef-labs.com \
--set trino.gateway.storageClass=oci \
dataroaster-trino-controller/dataroaster-trino-controller;
```
* `trino.gateway.publicEndpoint`: public endpoint to which trino clients will connect to run queries via https.
* `trino.gateway.proxyHostName`: ingress host name of trino gateway for proxy.
* `trino.gateway.restHostName`: ingress host name of trino gateway for rest.
* `trino.gateway.storageClass`: storage class for trino gateway mysql.

`trino.gateway.proxyHostName` and `trino.gateway.restHostName` needs to be changed with your domain and registered to DNS server later.


This chart will create `Trino Controller` along with several components like this.
* nginx ingress controller
* cert manager
* [DataRoaster Trino Gateway](https://github.com/cloudcheflabs/dataroaster/tree/master/trino-ecosystem/trino-gateway)
* [DataRoaster Trino Operator](https://github.com/cloudcheflabs/dataroaster/tree/master/operators/trino)
* [DataRoaster Helm Operator](https://github.com/cloudcheflabs/dataroaster/tree/master/operators/helm)


### Add DNS records to DNS Server

Add dns records of `trino.gateway.proxyHostName` and `trino.gateway.restHostName` to DNS server like this.

```
trino-gateway-proxy-test.cloudchef-labs.com ---> 146.56.150.205
trino-gateway-rest-test.cloudchef-labs.com  ---> 146.56.150.205
```
, where `146.56.150.205` is external ip of nginx service.


### Renew Trino Gateway Certificates for Ingresses

You have to renew trino gateway certificates for ingresses to get valid certificates from let's encrypt.

As seen below, all the certificates created in trino gateway for now are not valid.
```
kubectl get cert -n trino-gateway;
NAME                                              READY   SECRET                                            AGE
trino-gateway-proxy-test.cloudchef-labs.com-tls   False   trino-gateway-proxy-test.cloudchef-labs.com-tls   18m
trino-gateway-rest-test.cloudchef-labs.com-tls    False   trino-gateway-rest-test.cloudchef-labs.com-tls    18m
```


#### Install cmctl

```
sudo yum install go -y;

OS=$(go env GOOS); ARCH=$(go env GOARCH); curl -sSL -o cmctl.tar.gz https://github.com/cert-manager/cert-manager/releases/download/v1.7.2/cmctl-$OS-$ARCH.tar.gz
tar xzf cmctl.tar.gz
sudo mv cmctl /usr/local/bin;
```

#### Renew Certificates.

```
cmctl renew --all -n trino-gateway;
```



To show the status of certificate.

```
cmctl status certificate trino-gateway-proxy-test.cloudchef-labs.com-tls -n trino-gateway;
```



## Demonstration

To demonstrate trino gateway, first port-forward trino controller service.
```
kubectl port-forward svc/trino-controller-service 8093 -n trino-controller;
```

### Create trino clusters.

```
# create trino cluster 1.
curl -XPOST \
http://localhost:8093/v1/trino/create \
-d  "name=etl-1" \
-d  "namespace=trino-etl-1" \
-d  "max_heap_size=8G" \
-d  "replicas=3" \
-d  "min_replicas=3" \
-d  "max_replicas=5" \
-d  "storage_class=oci" \
;


# create trino cluster 2.
curl -XPOST \
http://localhost:8093/v1/trino/create \
-d  "name=etl-2" \
-d  "namespace=trino-etl-2" \
-d  "max_heap_size=8G" \
-d  "replicas=3" \
-d  "min_replicas=3" \
-d  "max_replicas=5" \
-d  "storage_class=oci" \
;


# create trino cluster 3.
curl -XPOST \
http://localhost:8093/v1/trino/create \
-d  "name=etl-3" \
-d  "namespace=trino-etl-3" \
-d  "max_heap_size=8G" \
-d  "replicas=3" \
-d  "min_replicas=3" \
-d  "max_replicas=5" \
-d  "storage_class=oci" \
;
```

`storage_class` needs to changed to suit to your kubernetes cluster.


### Register trino clusters to trino gateway

#### Create cluster group

```
curl -XPOST \
http://localhost:8093/v1/cluster_group/create \
-d  "group_name=etl_group";
```

#### Register users

```
## register user who belongs to the cluster group.
curl -XPOST \
http://localhost:8093/v1/users/create \
-d  "user=trino" \
-d  "password=trino123" \
-d  "group_name=etl_group";
```

#### Register trino clusters
We need register three trino coordinator services.

```
# register cluster 1.
curl -XPOST \
http://localhost:8093/v1/cluster/create \
-d  "cluster_name=etl-1" \
-d  "cluster_type=etl" \
-d  "url=http://trino-coordinator-service.trino-etl-1.svc:8080" \
-d  "activated=true" \
-d  "group_name=etl_group";

# register cluster 2.
curl -XPOST \
http://localhost:8093/v1/cluster/create \
-d  "cluster_name=etl-2" \
-d  "cluster_type=etl" \
-d  "url=http://trino-coordinator-service.trino-etl-2.svc:8080" \
-d  "activated=true" \
-d  "group_name=etl_group";


# register cluster 3.
curl -XPOST \
http://localhost:8093/v1/cluster/create \
-d  "cluster_name=etl-3" \
-d  "cluster_type=etl" \
-d  "url=http://trino-coordinator-service.trino-etl-3.svc:8080" \
-d  "activated=true" \
-d  "group_name=etl_group";
```




### Run queries with client

Trino client will be used to run queries to trino gateway.

#### Install trino client.

```
mkdir -p ~/trino-cli;
cd ~/trino-cli;

curl -L -O https://repo1.maven.org/maven2/io/trino/trino-cli/384/trino-cli-384-executable.jar;
mv trino-cli-384-executable.jar trino
chmod +x trino

./trino --version;
```

#### Connect to trino gateway

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






## Trino Controller REST API


### Trino Cluster Installation

#### List trino clusters
Parameters: NO

```
curl -XGET \
http://localhost:8093/v1/trino/list;
```


#### Create trino cluster
Parameters:
* `name` : unique cluster name.
* `namespace` : trino cluster namespace.
* `max_heap_size` : max heap size of coordiantor and workers.
* `replicas` : trino worker replica count.
* `min_replicas` : worker hpa min. replica count.
* `max_replicas` : worker hpa max. replica count.
* `trino_image` : trino image, optional.

```
curl -XPOST \
http://localhost:8093/v1/trino/create \
-d  "name=etl-1" \
-d  "namespace=trino-etl-1" \
-d  "max_heap_size=8G" \
-d  "replicas=3" \
-d  "min_replicas=2" \
-d  "max_replicas=5" \
--data-urlencode "trino_image=trinodb/trino:389" \
;
```


#### Delete trino cluster
Parameters:
* `name` : cluster name.

```
curl -XDELETE \
http://localhost:8093/v1/trino/delete \
-d  "name=etl-1";
```

### Trino Pod Template

#### Update Trino Pod Template
Parameters:
* `name` : unique cluster name.
* `coordinator_pod_template` : coordinator pod template in yaml. base64 encoded.
* `worker_pod_template` : worker pod template in yaml. base64 encoded.

```
cat <<EOF > coordinator-pod-template.yaml
affinity:
  nodeAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      nodeSelectorTerms:
        - matchExpressions:
            - key: coordinator
              operator: In
              values:
                - "true"
            - key: worker
              operator: NotIn
              values:
                - "true"
            - key: cluster-name
              operator: In
              values:
                - "etl-1"
            - key: management
              operator: NotIn
              values:
                - "true"
  podAntiAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      - labelSelector:
          matchExpressions:
            - key: component
              operator: In
              values:
                - "coordinator"
tolerations:
  - key: "cluster-name"
    operator: "Equal"
    value: "etl-1"
    effect: "NoSchedule"
EOF

cat <<EOF > worker-pod-template.yaml
affinity:
  nodeAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      nodeSelectorTerms:
        - matchExpressions:
            - key: coordinator
              operator: NotIn
              values:
                - "true"
            - key: worker
              operator: In
              values:
                - "true"
            - key: cluster-name
              operator: In
              values:
                - "etl-1"
            - key: management
              operator: NotIn
              values:
                - "true"
  podAntiAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      - labelSelector:
          matchExpressions:
            - key: component
              operator: In
              values:
                - "worker"
tolerations:
  - key: "cluster-name"
    operator: "Equal"
    value: "etl-1"
    effect: "NoSchedule"
EOF



curl -XPUT \
http://localhost:8093/v1/trino/pod-template/update \
-d  "name=etl-1" \
-d "coordinator_pod_template=$(base64 -w 0 ./coordinator-pod-template.yaml)" \
-d "worker_pod_template=$(base64 -w 0 ./worker-pod-template.yaml)" \
;
```



### Trino Configuration


#### List trino cluster configs
Parameters: NO

```
curl -XGET \
http://localhost:8093/v1/trino/config/list;
```


#### Create trino cluster config
Parameters:
* `name` : cluster name.
* `coordinator_config_name` : coordinator config name.
* `coordinator_config_path` : coordinator config path.
* `coordinator_config_value` :  coordinator config value which is base64 encoded.
* `worker_config_name` : worker config name.
* `worker_config_path` :  worker config path.
* `worker_config_value` : worker config value which is base64 encoded.

```
cat <<EOF > hive.properties
connector.name=hive-hadoop2
hive.metastore.uri=thrift://metastore.hive-metastore.svc:9083
hive.allow-drop-table=true
hive.max-partitions-per-scan=1000000
hive.compression-codec=NONE
hive.s3.path-style-access=true
hive.s3.ssl.enabled=true
hive.s3.max-connections=100
hive.s3.endpoint=endpoint
hive.s3.aws-access-key=acces-key
hive.s3.aws-secret-key=secret-key
EOF




curl -XPOST \
http://localhost:8093/v1/trino/config/create \
-d  "name=etl-1" \
-d  "coordinator_config_name=hive.properties" \
-d  "coordinator_config_path=/etc/trino/catalog" \
-d "coordinator_config_value=$(base64 -w 0 ./hive.properties)" \
-d  "worker_config_name=hive.properties" \
-d  "worker_config_path=/etc/trino/catalog" \
-d "worker_config_value=$(base64 -w 0 ./hive.properties)" \
;
```


#### Update trino cluster config
Parameters:
* `name` : cluster name.
* `coordinator_config_name` : coordinator config name.
* `coordinator_config_path` : coordinator config path.
* `coordinator_config_value` :  coordinator config value which is base64 encoded.
* `worker_config_name` : worker config name.
* `worker_config_path` :  worker config path.
* `worker_config_value` : worker config value which is base64 encoded.

```
cat <<EOF > hive.properties
connector.name=hive-hadoop2
hive.metastore.uri=thrift://metastore.hive-metastore.svc:9083
hive.allow-drop-table=true
hive.max-partitions-per-scan=1000000
hive.compression-codec=NONE
hive.s3.path-style-access=true
hive.s3.ssl.enabled=true
hive.s3.max-connections=1000
hive.s3.endpoint=endpoint
hive.s3.aws-access-key=access-key
hive.s3.aws-secret-key=secret-key
EOF




curl -XPUT \
http://localhost:8093/v1/trino/config/update \
-d  "name=etl-1" \
-d  "coordinator_config_name=hive.properties" \
-d  "coordinator_config_path=/etc/trino/catalog" \
-d "coordinator_config_value=$(base64 -w 0 ./hive.properties)" \
-d  "worker_config_name=hive.properties" \
-d  "worker_config_path=/etc/trino/catalog" \
-d "worker_config_value=$(base64 -w 0 ./hive.properties)" \
;
```




#### Delete trino cluster config
Parameters:
* `name` : cluster name.
* `coordinator_config_name` : coordinator config name.
* `coordinator_config_path` : coordinator config path.
* `worker_config_name` : worker config name.
* `worker_config_path` :  worker config path.

```
curl -XDELETE \
http://localhost:8093/v1/trino/config/delete \
-d  "name=etl-1" \
-d  "coordinator_config_name=hive.properties" \
-d  "coordinator_config_path=/etc/trino/catalog" \
-d  "worker_config_name=hive.properties" \
-d  "worker_config_path=/etc/trino/catalog" \
;
```


### Trino Worker Scale out

#### List worker count
Parameters: NO

```
curl -XGET \
http://localhost:8093/v1/scale/list_worker_count;
```

#### Scale out workers
Parameters:
* `name` : cluster name.
* `replicas` : worker replica count.

```
curl -XPUT \
http://localhost:8093/v1/scale/scale_workers \
-d "name=etl-1" \
-d "replicas=4" \
;
```

#### List Worker HPA
Parameters: NO

```
curl -XGET \
http://localhost:8093/v1/scale/list_hpa;
```

#### Update Worker HPA
Parameters:
* `name` : cluster name.
* `min_replicas` : min. worker replica count.
* `max_replicas` : max. worker replica count.

```
curl -XPUT \
http://localhost:8093/v1/scale/scale_hpa \
-d "name=etl-1" \
-d "min_replicas=4" \
-d "max_replicas=8" \
;
```

### Register Trino Clusters

#### Create Cluster Group
Parameters:
* `group_name` : cluster group name.

```
curl -XPOST \
http://localhost:8093/v1/cluster_group/create \
-d  "group_name=etl_group";
```

#### List cluster groups
Parameters: NO

```
curl -XGET \
http://localhost:8093/v1/cluster_group/list;
```


#### Delete cluster group
Parameters:
* `group_name` : cluster group name.

```
curl -XDELETE \
http://localhost:8093/v1/cluster_group/delete \
-d  "group_name=etl_group";
```


#### Create User
Parameters:
* `user` : Unique user name.
* `password` : Password.
* `group_name` : Cluster group name to which the user belongs.

```
curl -XPOST \
http://localhost:8093/v1/users/create \
-d  "user=trino" \
-d  "password=trino123" \
-d  "group_name=etl_group";
```


#### Update User Password
Parameters:
* `user` : User name.
* `password` : Updated password.

```
curl -XPUT \
http://localhost:8093/v1/users/update/password \
-d  "user=trino" \
-d  "password=trino123updated";
```


#### List users
Parameters: NO

```
curl -XGET \
http://localhost:8093/v1/users/list;
```


#### Delete user
Parameters:
* `user` : User name.

```
curl -XDELETE \
http://localhost:8093/v1/users/delete \
-d  "user=trino";
```



#### Register trino cluster
Parameters:
* `cluster_name` : Unique cluster name.
* `cluster_type` : Trino cluster type, for instance `etl`, `interactive`, `scheduled` .
* `url` : Backend Trino URL
* `activated` :  Status of trino cluster. `true` or `false`.
* `group_name` : Cluster group name to which the cluster belongs.

```
curl -XPOST \
http://localhost:8093/v1/cluster/create \
-d  "cluster_name=etl-1" \
-d  "cluster_type=etl" \
-d  "url=http://trino-coordinator-service.trino-etl-1.svc:8080" \
-d  "activated=true" \
-d  "group_name=etl_group";
```


#### Update trino cluster activated
Parameters:
* `cluster_name` : Cluster name.
* `activated` :  Status of trino cluster. `true` or `false`.

```
curl -XPUT \
http://localhost:8093/v1/cluster/update/activated \
-d  "cluster_name=etl-1" \
-d  "activated=false";
```


#### List registered trino clusters
Parameters: NO

```
curl -XGET \
http://localhost:8093/v1/cluster/list;
```


#### Deregister trino cluster
Parameters:
* `cluster_name` : Cluster name.

```
curl -XDELETE \
http://localhost:8093/v1/cluster/delete \
-d  "cluster_name=etl-1";
```


## Publishment
* [Trino Gateway](https://itnext.io/trino-gateway-8e654366df5e)
