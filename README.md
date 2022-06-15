# DataRoaster
DataRoaster is open source data platform running on Kubernetes. Data platform components like Hive metastore, Spark Thrift Server(Hive on Spark), Trino, Redash, JupyterHub etc. will be deployed on Kubernetes using DataRoaster easily.

## DataRoaster Architecture

![DataRoaster Architecture](http://www.cloudchef-labs.com/images/architecture.png)

DataRoaster consists of the following components.
* CLI: command line interface to API Server.
* API Server: handles requests from clients like CLI.
* Authorizer: runs as OAuth2 Server.
* Secret Manager: manages secrets like kubeconfig using Vault.
* Resource Controller: manages remote kubernetes resources with kubectl, helm and kubernetes client like fabric8 k8s client.



## Services provided by DataRoaster

### Data Catalog
* Hive Metastore: Standard Data Catalog in Data Lake

### Query Engine
* Spark Thrift Server: used as Hive Server, namely Hive on Spark. Interface to query data in Data Lake
* Trino: Fast Interactive Query Engine to query data in Data Lake

### Streaming
* Kafka: Popular Streaming Platform

### Analytics
* JupyterHub: Controller to serve Jupyter Notebook which is most popular web based interactive analytics tool for multiple users
* Redash: Visual Data Analytics SQL Engine which provides a lot of data sources connectors

### Workflow
* Argo Workflow: Workflow engine running on Kubernetes, with which containerized long running batch jobs, ETL Jobs, ML Jobs, etc can be scheduled to run on Kubernetes

### CI / CD
* Jenkins: Popular continuous Integration Server
* Argo CD: Continuous Delivery tool for Kubernetes

### Metrics Monitoring
* Prometheus: Popular monitoring tool
* Grafana: Popular metrics visibility tool

### Pod Log Monitoring
* ELK: Elasticsearch, Logstash and Kibana
* Filebeat: used to fetch log files

### Distributed Tracing
* Jaeger: Popular microservices distributed tracing platform

### Backup
* Velero: used to backup Kubernetes Resources and Persistent Volumes

### Private Registry
* Harbor: used as private registry to manage docker images and helm charts

### Ingress Controller
* Ingress Controller NGINX: Popular Ingress Controller
* Cert Manager: manage certificates for ingress resources


## DataRoaster Kubernetes Version Matrix
| DataRoaster | Kubernetes  | 
| ------- | --- | 
| >=3.0.3 | 1.18.10 / 1.19.12 / 1.20.8 | 
| 3.0.1 | 1.17.9 | 


## DataRoaster Component Version Matrix

### DataRoaster >= 3.0.1
| Service | Component | Application Version  | Helm Chart Repo / Version |
| ------- | ------- | --- | ---------------- |
| Data Catalog | Hive Metastore | 3.0.0 |  |
| Query Engine | Spark Thrift Server | Spark 3.0.3 |   |
| Query Engine | Trino | 360 |   |
| Analytics | Redash | 10.0.0-beta.b49597 |   |
| Analytics | JupyterHub | 1.4.2 | https://jupyterhub.github.io/helm-chart/ <br /> 1.1.3 |
| Streaming | Kafka | 2.8.0 | https://charts.bitnami.com/bitnami <br /> 13.0.4 |
| Workflow | Argo Workflow | 3.0.10 |   |
| CI / CD | Jenkins | 2.289.1 |   |
| CI / CD | Argo CD | 2.0.3 |   |
| Metrics Monitoring | Metrics Server | 0.4.1 |   |
| Metrics Monitoring | Prometheus Stack | 0.43.2 | https://prometheus-community.github.io/helm-charts <br /> 12.2.4 |
| Pod Log Monitoring | Filebeat | 7.12.1 | |
| Pod Log Monitoring | Logstash | 7.12.1 | |
| Distributed Tracing | Jaeger | 1.22.0 | https://jaegertracing.github.io/helm-charts <br /> 0.46.0 |
| Backup | Velero | 1.6.0 | |
| Private Registry | Harbor | 2.1.5 | https://helm.goharbor.io <br /> 1.5.5 |
| Ingress Controller | Ingress NGINX | 0.46.0 | https://kubernetes.github.io/ingress-nginx <br /> 3.32.0 |
| Ingress Controller | Cert Manager | 1.0.1 | |




## DataRoaster Demo
This demo shows how to create the components like hive metastore, spark thrift server, trino, redash and jupyterhub deployed on Kubernetes with ease using DataRoaster.

[![DataRoaster Demo](http://www.cloudchef-labs.com/images/demo-thumbnail.jpg)](https://youtu.be/AeqkkQDwPqY "DataRoaster Demo")






## Install DataRoaster with ansible
With dataroaster ansible playbook, dataroaster will be installed automatically.

The following components will be installed with dataroaster ansible playbook.
* JDK 1.8
* Maven
* Kubectl
* Helm
* MySQL Server
* Vault
* DataRoaster API Server
* DataRoaster Authorizer
* DataRoaster CLI


See the video below about installing DataRoaster with ansible.

<div align="left">
  <a href="https://youtu.be/9mqVkrLOu3Y">
	<img 
	src="http://www.cloudchef-labs.com/images/install-dataroaster.png" 
	alt="Install DataRoaster with Ansible" 
	style="width:35%;">
  </a>
</div>


### Download and extract ansible playbook for dataroaster installation
```
curl -L -O https://github.com/cloudcheflabs/dataroaster/releases/download/3.0.5/dataroaster-ansible-3.0.5-dist.tgz
tar zxvf dataroaster-ansible-3.0.5-dist.tgz
cd dataroaster/
```

### Edit inventory
Edit the file `inventory/dataroaster.ini`
```
...
[all]
dataroaster ansible_ssh_host=<ip-address> ip=<ip-address>
...
```
`<ip-address>` is the ip address of the machine where dataroaster will be installed.

### Run ansible playbook
Now, you can run ansible playbook to install/uninstall/reinstall/start/stop/restart DataRoaster automatically.
The following `<sudo-user>` is sudo user who will execute ansible playbook on local and remote machine.

#### Install
```
ansible-playbook -i inventory/dataroaster.ini install-all.yml \
--extra-vars "exec_user=<sudo-user> target_hosts=all";
```

You will meet the prompts while installing vault.
```
...
              "stdout_lines": [
                    "Unseal Key 1: QZ27JD9nJOPQLozKUvbwdHSTHKafOprwT4xw+RGUxBLI",
                    "Unseal Key 2: GxnjXc5IHo3vRuh8boQD+u4FZM7nW+Y5xpWRXTSXfHBe",
                    "Unseal Key 3: phA5yLU2csyAME9e8H+3NzmYq7ypilksIzLxkanmKUvl",
                    "Unseal Key 4: BVZx/+hL6MLYcwkvONFD3CXZj8ND2yAlSPrvZ6+3lRN9",
                    "Unseal Key 5: etU5dE+Nn+tYztFqoffUOJPQc5vy4RZuinAghI8RHVUH",
                    "",
                    "Initial Root Token: s.M6MNcOX92nAZjEwH5u4yVkbn",
                    "",
                    "Vault initialized with 5 key shares and a key threshold of 3. Please securely",
                    "distribute the key shares printed above. When the Vault is re-sealed,",
                    "restarted, or stopped, you must supply at least 3 of these keys to unseal it",
                    "before it can start servicing requests.",
                    "",
                    "Vault does not store the generated master key. Without at least 3 key to",
                    "reconstruct the master key, Vault will remain permanently sealed!",
                    "",
                    "It is possible to generate new unseal keys, provided you have a quorum of",
                    "existing unseal keys shares. See \"vault operator rekey\" for more information."
                ]
...
TASK [vault/install : prompt for unseal vault 1] *********************************************************************************************************************************************
[WARNING]: conditional statements should not include jinja2 templating delimiters such as {{ }} or {% %}. Found: ("{{ run_option }}" == "reinstall")
[vault/install : prompt for unseal vault 1]
Enter 1. Unseal Key :
```
Because thease generated unseal keys and initial root token of vault cannot be obtained again, you have to copy them to your file. Enter the unseal keys and initial root token of vault for the prompts.

You will also encounter the prompt to enter vault init. root token while installing apiserver like this:
```
...
TASK [apiserver/install : prompt for vault initial root token] *******************************************************************************************************************************
[WARNING]: conditional statements should not include jinja2 templating delimiters such as {{ }} or {% %}. Found: ("{{ run_option }}" == "reinstall")
[apiserver/install : prompt for vault initial root token]
Enter vault initial root token :
```
Enter initial root token of vault which you have obtained above.


After installation success, api server and authorizer can be found in `/opt/dataroaster`, and log files of api server can be found in `/data/dataroaster/logs/apiserver`.


#### Uninstall
```
ansible-playbook -i inventory/dataroaster.ini uninstall-all.yml \
--extra-vars "exec_user=<sudo-user> target_hosts=all";
```

#### Reinstall
```
ansible-playbook -i inventory/dataroaster.ini reinstall-all.yml \
--extra-vars "exec_user=<sudo-user> target_hosts=all";
```

#### Start
```
ansible-playbook -i inventory/dataroaster.ini start-all.yml \
--extra-vars "exec_user=<sudo-user> target_hosts=all";
```

#### Stop
```
ansible-playbook -i inventory/dataroaster.ini stop-all.yml \
--extra-vars "exec_user=<sudo-user> target_hosts=all";
```

#### Restart
```
ansible-playbook -i inventory/dataroaster.ini restart-all.yml \
--extra-vars "exec_user=<sudo-user> target_hosts=all";
```


## Storage Requirement

Currently, storage service to provision external storages like ceph / minio and install storage classes on kubernetes is not provided by DataRoaster yet, which will be supported by future release of DataRoaster. Before getting started, you should take a look at the following instruction.

### Storage Class
Most of the components provided by DataRoaster will be deployed as statefulset on kubernetes, so storage classes should be installed on your kubernetes cluster to provision persistent volumes automatically.
If you use managed kubernetes services provided by public cloud providers, you don't have to install storage classes for most of cases, but if your kubernetes cluster is installed in on-prem environment, you have to install storage class on your kubernetes cluster for yourself. 
For instance, if you have installed ceph as external storage, ceph storage class can be installed on your kubernetes cluster, see this blog how to do it: https://itnext.io/provision-volumes-from-external-ceph-storage-on-kubernetes-and-nomad-using-ceph-csi-7ad9b15e9809.

There is a component like spark thrift server which needs to use `ReadWriteMany` supported storage class, for instance, `nfs` to save intermediate data on PVs.
To install `nfs` storage class, run the following helm chart.
```
cd <dataroaster-src>/components/nfs/nfs-server-provisioner-1.1.1;

helm install \
nfs-server . \
--set replicaCount=1 \
--set namespace=nfs \
--set persistence.enabled=true \
--set persistence.size=1000Gi \
--set persistence.storageClass=<storage-class>;
```
`<storage-class>` can be `ReadWriteOnce` supported storage class already installed on your kubernetes cluster.

### S3 Object Storage
S3 compatible object storage will be also required to save data for several components provided by DataRoaster. There are many S3 compatible object storages out there, for example you can use the following:
* MinIO: Popular S3 compatible object storage, see https://min.io/
* Ceph S3 compatible object storage: ceph provides S3 API, that is, ceph can be used as S3 compatible object storage. See https://docs.ceph.com/en/latest/radosgw/
* AWS S3: aws s3 object storage.



## Getting started

### Step 1: Login to API Server
```
dataroaster login http://localhost:8082;
...

# user / password: dataroaster / dataroaster123
```

### Step 2: Create Kubernetes Cluster
```
dataroaster cluster create --name my-cluster --description my-cluster-desc;
```

### Step 3: Register kubeconfig file for your kubernetes cluster
```
dataroaster kubeconfig create --kubeconfig ~/.kube/config
```

### Step 4: Create Project where services will be created
```
dataroaster project create  --name my-project --description my-project-desc;
```

### Step 5: Create Ingress Controller NGINX and Cert Manager in your kubernetes cluster
All the ingresses of DataRoaster services will be created with this ingress controller,
and all the certificates for the ingresses will be managed by this cert manager.
```
dataroaster ingresscontroller create;
```

### Step 6: Create Services like Data Catalog, Query Engine, etc in your Project
See [DataRoaster CLI usage](https://github.com/cloudcheflabs/dataroaster#dataroaster-cli-usage) how to create services.

## Usage Example: DataRoaster Demo
As shown in dataroaster demo video above, the architecture of the demo looks like this.

![Demo Architecture](https://miro.medium.com/max/1400/1*5htePIy2DKpuzFuI7wWU9g.png)

For this demo, ceph storage as external storage has been used by which ceph storage class has been installed on kubernetes. Ceph also provides S3 API and can be used as s3 compatible object storage.

The scenario of the demo is:
* create parquet table in s3 compatible object storage which is provided by ceph storage with running spark example job using hive metastore.
* query data in parquet table saved in ceph using spark thrift server and trino which use hive metastore.
* query data with the connectors to spark thrift server and trino coordinator from redash and jupyter.

### Create Data Catalog
Hive metastore will be created.

```
# create.
dataroaster datacatalog create \
--s3-bucket mykidong \
--s3-access-key TOW32G9ULH63MTUI6NNW \
--s3-secret-key jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint https://ceph-rgw-test.cloudchef-labs.com \
--storage-size 1;


# delete.
dataroaster datacatalog delete;
```

### Create Query Engine
Spark thrift server(hive on spark) and trino will be created. Both of them depends on hive metastore which needs to be installed on your kubernetes cluster before.

```
# create.
dataroaster queryengine create \
--s3-bucket mykidong \
--s3-access-key TOW32G9ULH63MTUI6NNW \
--s3-secret-key jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint https://ceph-rgw-test.cloudchef-labs.com \
--spark-thrift-server-executors 1 \
--spark-thrift-server-executor-memory 1 \
--spark-thrift-server-executor-cores 1 \
--spark-thrift-server-driver-memory 1 \
--trino-workers 3 \
--trino-server-max-memory 16 \
--trino-cores 1 \
--trino-temp-data-storage 1 \
--trino-data-storage 1;

# delete.
dataroaster queryengine delete;
```

### Create Parquet Table using Spark Example Job
This is simple spark job to create parquet table in ceph s3 object storage using hive metastore.

Create hive metastore service to be accessed from local spark job.
```
# create hive metastore service with the type of load balancer to be accessed by example spark job on local.
cat <<EOF > hive-metastore-service.yaml
---
apiVersion: v1
kind: Service
metadata:
  name: metastore-service
  namespace: dataroaster-hivemetastore
spec:
  type: LoadBalancer
  ports:
  - port: 9083
  selector:
    app: metastore
EOF

kubectl apply -f hive-metastore-service.yaml;
```

Run spark job.
```
# build dataroaster source.
cd <dataroaster-src>;
mvn -e -DskipTests=true clean install;

# run spark job.
cd components/hive/spark-thrift-server;
mvn -e -Dtest=JsonToParquetTestRunner \
-DmetastoreUrl=$(kubectl get svc metastore-service -n dataroaster-hivemetastore -o jsonpath={.status.loadBalancer.ingress[0].ip}):9083 \
-Ds3Bucket=mykidong \
-Ds3AccessKey=TOW32G9ULH63MTUI6NNW \
-Ds3SecretKey=jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
-Ds3Endpoint=https://ceph-rgw-test.cloudchef-labs.com \
test;
```

### Query Data using CLI

#### Connect to Spark Thrift Server using Beeline
Query data in parquet table created by the spark job above with the connection to spark thrift server using beeline.

```
cd ${SPARK_HOME};
export SPARK_THRIFT_SERVER_NAMESPACE=dataroaster-spark-thrift-server;
bin/beeline -u jdbc:hive2://$(kubectl get svc spark-thrift-server-service -n ${SPARK_THRIFT_SERVER_NAMESPACE} -o jsonpath={.status.loadBalancer.ingress[0].ip}):10016;

...
# query data.
show tables;
select * from test_parquet;
select count(*) from test_parquet;
...

```

#### Connect to Trino Coordinator using Trino CLI
Query data in parquet table created by the spark job above with the connection to trino coordinator using trino cli.

```
kubectl exec -it trino-cli -n dataroaster-trino -- /opt/trino-cli --server trino-coordinator:8080 --catalog hive --schema default;

...
# query data.
show tables;
select * from test_parquet;
select count(*) from test_parquet;
...
```

### Create Analytics
Redash and jupyterhub will be created.

Before creating service, ingress host whose ip address is the external ip of ingress nginx service must be registered to your public dns server. To get external ip of ingress nginx service:
```
kubectl get svc -n ingress-nginx;
```
Github oauth secret also needs to be created to authenticate with github oauth service from jupyterhub.

```
# create.
dataroaster analytics create \
--jupyterhub-github-client-id any-github-client-id \
--jupyterhub-github-client-secret any-github-client-secret \
--jupyterhub-ingress-host jupyterhub-test.cloudchef-labs.com \
--jupyterhub-storage-size 1 \
--redash-storage-size 1;

# delete.
dataroaster analytics delete;
```

### Query Data from Redash and Jupyter

#### Query Data from Redash
Query data in parquet tables using hive connector to spark thrift server and trino connector to trino coordinator from redash.

```
# get external ip of redash loadbalancer.
kubectl get svc -n dataroaster-redash;

# redash ui
http://<external-ip>:5000/

# get external ip of trino service.
kubectl get svc -n dataroaster-trino;

# get external ip of spark thrift server service.
kubectl get svc -n dataroaster-spark-thrift-server;
```

#### Query Data from Jupyter
Query data in parquet table with trino connector to trino coordinator from jupyter.

```
# jupyterhub ui.
https://jupyterhub-test.cloudchef-labs.com/

# trino example in jupyter.

## get external ip of trino service.
kubectl get svc -n dataroaster-trino;

...
from pyhive import trino
host_name = "146.56.138.128"
port = 8080
protocol = "http"
user = "anyuser"
password = None
schema = "default"
catalog = "hive"
def trinoconnection(host_name, port, protocol, user, password, schema, catalog):
    conn = trino.connect(host=host_name, port=port, username=user, password=password, schema=schema, catalog=catalog)
    cur = conn.cursor()
    cur.execute('select * from test_parquet')
    result = cur.fetchall()	
    return result
	
# Call above function
output = trinoconnection(host_name, port, protocol, user, password, schema, catalog)
print(output)
...	
```
`host_name` needs to be replaced with external ip of trino service. To get the external ip of it:
```
kubectl get svc -n dataroaster-trino;
```

## Blueprint Deployment
Blueprint Deployment is used to deploy all the services defined in blueprint yaml on kubernetes all at once instead of creating services using individual cli commands.

The full format of blueprint example can be found in [here](https://github.com/cloudcheflabs/dataroaster/blob/master/cli/src/test/resources/blueprint/blueprint.yaml).

For example, with the following blueprint, services like ingress controller, data catalog(hive metastore), query engine(spark thrift server, trino), and analytics(redash, jupyterhub) will be created on kubernetes all at once.
```
project:
  name: my-blueprint-project
  description: "My blueprint project description..."
cluster:
  name: my-blueprint-cluster
  description: "My blueprint cluster description..."
  kubeconfig: "/home/opc/.kube/config"
services:
  - name: ingresscontroller

  - name: datacatalog
    params:
      properties:
        - s3
      storage-size: 1
    extra-params:
      storage-class:
        property-ref: storage-classes
        key: storage-class-rwo

  - name: queryengine
    params:
      properties:
        - s3
      spark-thrift-server-executors: 1
      spark-thrift-server-executor-memory: 1
      spark-thrift-server-executor-cores: 1
      spark-thrift-server-driver-memory: 1
      trino-workers: 3
      trino-server-max-memory: 16
      trino-cores: 1
      trino-temp-data-storage: 1
      trino-data-storage: 1
    extra-params:
      spark-thrift-server-storage-class:
        property-ref: storage-classes
        key: storage-class-rwx
      trino-storage-class:
        property-ref: storage-classes
        key: storage-class-rwo
    depends: datacatalog

  - name: analytics
    params:
      jupyterhub-github-client-id: any-client-id
      jupyterhub-github-client-secret: any-client-secret
      jupyterhub-ingress-host: jupyterhub-test.cloudchef-labs.com
      jupyterhub-storage-size: 1
      redash-storage-size: 1
    extra-params:
      storage-class:
        property-ref: storage-classes
        key: storage-class-rwo
    depends: ingresscontroller


properties:
  - name: s3
    kv:
      s3-bucket: mykidong
      s3-access-key: any-access-key
      s3-secret-key: any-secret-key
      s3-endpoint: https://s3-endpoint
  - name: storage-classes
    kv:
      storage-class-rwo: oci
      storage-class-rwx: nfs
```

To create services with blueprint:
```
dataroaster blueprint create --blueprint /home/opc/blueprint.yaml;
```

To delete services with blueprint:
```
dataroaster blueprint delete --blueprint /home/opc/blueprint.yaml;
```


## DataRoaster CLI Usage

### Login
Login to API server.

```
dataroaster login <server>
```
* `server`: API Server URL.


Example:
```
dataroaster login http://localhost:8082;
...

# user / password: dataroaster / dataroaster123
```


### Cluster
Register Kubernetes Clusters.

#### Create Cluster
```
dataroaster cluster create <params>
```
* `name`: kubernetes cluster name.
* `description`: description of the kubernetes cluster.

Example:
```
dataroaster cluster create --name dataroaster-cluster --description dataroaster-desc...;
```

#### Update Cluster
```
dataroaster cluster update;
```

#### Delete Cluster
```
dataroaster cluster delete;
```


### Kubeconfig
Upload kubeconfig file for the kubernetes cluster.

#### Create Kubeconfig
```
dataroaster kubeconfig create <params>
```
* `kubeconfig`: kubeconfig file path.

Example:
```
dataroaster kubeconfig create --kubeconfig ~/.kube/config
```

#### Update Kubeconfig
```
dataroaster kubeconfig update <params>
```
* `kubeconfig`: kubeconfig file path.

Example:
```
dataroaster kubeconfig update --kubeconfig ~/.kube/config
```


### Project
Manage Project where services will be created.

#### Create Project
```
dataroaster project create <params>
```
* `name`: kubernetes cluster name.
* `description`: description of the kubernetes cluster.

Example:
```
dataroaster project create  --name new-test-project --description new-test-desc;
```

#### Update Project
```
dataroaster project update;
```

#### Delete Project
```
dataroaster project delete;
```


### Ingress Controller
Manage Ingress Controller NGINX and Cert Manager.

#### Create Ingress Controller
Ingress controller nginx and cert manager will be created.
* The namespace of ingress controller nginx is `ingress-nginx`.
* The namespace of cert manager is `cert-manager`.
```
dataroaster ingresscontroller create;
```

#### Delete Ingress Controller
```
dataroaster ingresscontroller delete;
```


### Data Catalog
Manage Data Catalog.

#### Create Data Catalog
Hive metastore and mysql server will be created.
* The namespace of hive metastore is `dataroaster-hivemetastore`.
```
dataroaster datacatalog create <params>
```
* `s3-bucket`: s3 bucket for hive metastore warehouse.
* `s3-access-key`: s3 access key.
* `s3-secret-key`: s3 secret key.
* `s3-endpoint`: s3 endpoint.
* `storage-size`: mysql storage size in GiB.


Example:
```
dataroaster datacatalog create \
--s3-bucket mykidong \
--s3-access-key TOW32G9ULH63MTUI6NNW \
--s3-secret-key jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint https://ceph-rgw-test.cloudchef-labs.com \
--storage-size 1;
```



#### Delete Data Catalog
```
dataroaster datacatalog delete;
```

### Query Engine
Manage Query Engine.

#### Create Query Engine
Spark thrift server(hive on spark) and trino will be created.
* The namespace of spark thrift server is `dataroaster-spark-thrift-server`.
* The namespace of trino is `dataroaster-trino`.

Query engine service depends on Data Catalog servcice. Before creating query engine service, you have to create data catalog service above on your kubernetes cluster.

```
dataroaster queryengine create <params>
```
* `s3-bucket`: s3 bucket where spark thrift server jar will be uploaded.
* `s3-access-key`: s3 access key.
* `s3-secret-key`: s3 secret key.
* `s3-endpoint`: s3 endpoint.
* `spark-thrift-server-executors`: executor count of spark thrift server.
* `spark-thrift-server-executor-memory`: spark thrift server executor memory in GB.
* `spark-thrift-server-executor-cores`: spark thrift server executor core count.
* `spark-thrift-server-driver-memory`: spark thrift server driver memory in GB.
* `trino-workers`: trino worker count.
* `trino-server-max-memory`: trino server max. memory in GB.
* `trino-cores`: trino server core count.
* `trino-temp-data-storage`: trino temporary data storage size in GiB.
* `trino-data-storage`: trino data storage size in GB.

Example:
```
dataroaster queryengine create \
--s3-bucket mykidong \
--s3-access-key TOW32G9ULH63MTUI6NNW \
--s3-secret-key jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint https://ceph-rgw-test.cloudchef-labs.com \
--spark-thrift-server-executors 1 \
--spark-thrift-server-executor-memory 1 \
--spark-thrift-server-executor-cores 1 \
--spark-thrift-server-driver-memory 1 \
--trino-workers 3 \
--trino-server-max-memory 16 \
--trino-cores 1 \
--trino-temp-data-storage 1 \
--trino-data-storage 1;
```


#### Delete Query Engine
```
dataroaster queryengine delete;
```

### Streaming
Manage Streaming.

#### Create Streaming
Kafka will be created.
* The namespace of kafka is `dataroaster-kafka`.
```
dataroaster streaming create <params>
```
* `kafka-replica-count`: kafka node count.
* `kafka-storage-size`: kafka storage size in GiB.
* `zk-replica-count`: zookeeper node count.

Example:
```
dataroaster streaming create \
--kafka-replica-count 3 \
--kafka-storage-size 4 \
--zk-replica-count 3;
```

#### Delete Streaming
```
dataroaster streaming delete;
```

### Analytics
Manage Analytics.

#### Create Analytics
Redash and jupyterhub will be created.
* The namespace of redash is `dataroaster-redash`.
* The namespace of jupyterhub is `dataroaster-jupyterhub`.

Before creating service, ingress host whose ip address is the external ip of ingress nginx service must be registered to your public dns server. To get external ip of ingress nginx service:
```
kubectl get svc -n ingress-nginx;
```
Github oauth secret also needs to be created to authenticate with github oauth service from jupyterhub.

```
dataroaster analytics create <params>
```
* `jupyterhub-github-client-id`: jupyterhub github oauth client id.
* `jupyterhub-github-client-secret`: jupyterhub github oauth client secret.
* `jupyterhub-ingress-host`: jupyterhub ingress host name.
* `jupyterhub-storage-size`: storage size in GiB of single jupyter instance.
* `redash-storage-size`: redash database storage size in GiB.

Example:
```
dataroaster analytics create \
--jupyterhub-github-client-id any-github-client-id \
--jupyterhub-github-client-secret any-github-client-secret \
--jupyterhub-ingress-host jupyterhub-test.cloudchef-labs.com \
--jupyterhub-storage-size 1 \
--redash-storage-size 1;
```

#### Delete Analytics
```
dataroaster analytics delete;
```

### Workflow
Manage Workflow.

#### Create Workflow
Argo Workflow will be created.
* The namespace of argo workflow is `dataroaster-argo-workflow`.
```
dataroaster workflow create <params>
```
* `storage-size`: database storage size in GiB.
* `s3-bucket`: s3 bucket where application logs of workflow will be saved.
* `s3-access-key`: s3 access key.
* `s3-secret-key`: s3 secret key.
* `s3-endpoint`: s3 endpoint.

Example:
```
dataroaster workflow create \
--storage-size 1 \
--s3-bucket mykidong \
--s3-access-key TOW32G9ULH63MTUI6NNW \
--s3-secret-key jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint ceph-rgw-test.cloudchef-labs.com;
```

Note that `s3-endpoint` has no such `https://` prefix.

#### Delete Workflow
```
dataroaster workflow delete;
```



### Pod Log Monitoring
Manage Pod Log Monitoring.

#### Create Pod Log Monitoring
Logstash and filebeat will be created.
* The namespace of filebeat is `dataroaster-filebeat`.
* The namespace of logstash is `dataroaster-logstash`.
```
dataroaster podlogmonitoring create <params>
```
* `elasticsearch-hosts`: List of Elasticsearch hosts.

Example:
```
dataroaster podlogmonitoring create \
--elasticsearch-hosts 192.168.10.10:9200,192.168.10.134:9200,192.168.10.145:9200;
```

#### Delete Pod Log Monitoring
```
dataroaster podlogmonitoring delete;
```


### Metrics Monitoring
Manage Metrics Monitoring.

#### Create Metrics Monitoring
Prometheus, grafana, metrics server will be created.
* The namespace of prom stack is `dataroaster-prom-stack`.
```
dataroaster metricsmonitoring create;
```

#### Delete Metrics Monitoring
```
dataroaster metricsmonitoring delete;
```

### Distributed Tracing
Manage Distributed Tracing.

#### Create Distributed Tracing
Jaeger will be created.
* The namespace of jaeger is `dataroaster-jaeger`.

Before creating service, ingress host whose ip address is the external ip of ingress nginx service must be registered to your public dns server. To get external ip of ingress nginx service:
```
kubectl get svc -n ingress-nginx;
```

```
dataroaster distributedtracing create <params>
```
* `ingress-host`: Host name of jaeger ingress.
* `elasticsearch-host-port`: an elasticsearch host and port.

Example:
```
dataroaster distributedtracing create \
--ingress-host ingress-nginx-jaeger-test.cloudchef-labs.com \
--elasticsearch-host-port 192.168.10.10:9200;
```

#### Delete Distributed Tracing
```
dataroaster distributedtracing delete;
```

### Private Registry
Manage private registry for docker images and helm charts.


#### Create Private Registry
Harbor will be created.
* The namespace of harbor is `dataroaster-harbor`.

Before creating service, ingress host whose ip address is the external ip of ingress nginx service must be registered to your public dns server. To get external ip of ingress nginx service:
```
kubectl get svc -n ingress-nginx;
```

```
dataroaster privateregistry create <params>
```
* `core-host`: Harbor core ingress host name.
* `notary-host`: Harbor notary ingress host name.
* `registry-storage-size`: regisry storage size in GiB.
* `chartmuseum-storage-size`: chart museum storage size in GiB.
* `jobservice-storage-size`: job service storage size in GiB.
* `database-storage-size`: database storage size in GiB.
* `redis-storage-size`: redis storage size in GiB.
* `trivy-storage-size`: trivy storage size in GiB.
* `s3-bucket`: name of s3 bucket where artifacts of harbor will be saved.
* `s3-access-key`: s3 access key.
* `s3-secret-key`: s3 secret key.
* `s3-endpoint`: s3 endpoint.

Example:
```
dataroaster privateregistry create \
--core-host harbor-core-test.cloudchef-labs.com \
--notary-host harbor-notary-test.cloudchef-labs.com \
--registry-storage-size 5 \
--chartmuseum-storage-size 5 \
--jobservice-storage-size 1 \
--database-storage-size 1 \
--redis-storage-size 1 \
--trivy-storage-size 5 \
--s3-bucket harbor \
--s3-access-key TOW32G9ULH63MTUI6NNW \
--s3-secret-key jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint https://ceph-rgw-test.cloudchef-labs.com;
```

#### Delete Private Registry
```
dataroaster privateregistry delete;
```

### CI / CD
Manage CI / CD.

#### Create CI / CD
Argo cd and jenkins will be created.
* The namespace of argo cd is `dataroaster-argocd`.
* The namespace of jenkins is `dataroaster-jenkins`.
```
dataroaster cicd create <params>
```
* `argocd-ingress-host`: ingress host name of Argo CD.
* `jenkins-ingress-host`: ingress host name of Jenkins.

Example:
```
dataroaster cicd create \
--argocd-ingress-host argocd-test.cloudchef-labs.com \
--jenkins-ingress-host jenkins-test.cloudchef-labs.com;
```

#### Delete CI / CD
```
dataroaster cicd delete;
```

### Backup
Manage Backup for Persistent Volumes and resources.

#### Create Backup
Velero will be created.
* The namespace of velero is `dataroaster-velero`.

Currently, Velero CLI which is missing in ansible dataroaster installation needs to be installed manually like this.
```
mkdir -p velero;
cd velero/
curl -L -O https://github.com/vmware-tanzu/velero/releases/download/v1.6.0/velero-v1.6.0-linux-amd64.tar.gz
tar zxvf velero-v1.6.0-linux-amd64.tar.gz
cd velero-v1.6.0-linux-amd64/
sudo cp velero /usr/local/bin/velero

# check velero.
velero
```

```
dataroaster backup create <params>
```
* `s3-bucket`: s3 bucket for backup.
* `s3-access-key`: s3 access key.
* `s3-secret-key`: s3 secret key.
* `s3-endpoint`: s3 endpoint.

Example:
```
dataroaster backup create \
--s3-bucket velero-backups \
--s3-access-key TOW32G9ULH63MTUI6NNW \
--s3-secret-key jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint https://ceph-rgw-test.cloudchef-labs.com;
```

#### Delete Backup
```
dataroaster backup delete;
```


### Blueprint
Manage Blueprint deployment.

#### Create Blueprint
Deploy all the services defined in blueprint on Kubernetes all at once.

```
dataroaster blueprint create <params>;
```
* `blueprint`: Blueprint yaml file path.

Example:
```
dataroaster blueprint create --blueprint /home/opc/blueprint.yaml;
```

#### Delete Blueprint
```
dataroaster blueprint delete <params>;
```
* `blueprint`: Blueprint yaml file path.

Example:
```
dataroaster blueprint delete --blueprint /home/opc/blueprint.yaml;
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


## Community

* DataRoaster Community Mailing Lists: https://groups.google.com/g/dataroaster



## License
The use and distribution terms for this software are covered by the Apache 2.0 license.







