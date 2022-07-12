# DataRoaster
DataRoaster is open source data platform running on Kubernetes.

## DataRoaster Architecture

![DataRoaster Architecture](https://github.com/cloudcheflabs/dataroaster/blob/master/operators/dataroaster/dataroaster-operator/docs/images/dataroaster-architecture.jpg)

DataRoaster has a simple architecture. There are several operators in DataRoaster to install data platform components easily.
* `DataRoaster Operator` handles REST requests to create custom resources.
* After creating custom resources, `Helm Operator` and other operators like `Trino Operator` and `Spark Operator` will be notified and create data platform components.

## Data Platform Components supported by DataRoaster

Components supported by DataRoaster:
* `Hive Metastore`: standard data catalog in data lakehouses.
* `Spark Thrift Server`: hive server compatible interface through which hive queries will be executed on spark.
* `Trino`: popular query engine in data lakehouses.
* `Redash`: popular BI tool.
* `Jupyterhub`: multi-user version of jupyter notebook.
* `Kafka`: popular event streaming platform.
* `Airflow`: popular workflow.


## Install DataRoaster

```
helm repo add dataroaster-operator https://cloudcheflabs.github.io/dataroaster-operator-helm-repo/
helm repo update;

helm install \
dataroaster-operator \
--create-namespace \
--namespace dataroaster-operator \
--version v3.0.8 \
--set image=cloudcheflabs/dataroaster-operator:4.3.0 \
--set dataroastermysql.storage.storageClass=oci \
dataroaster-operator/dataroaster-operator;
```
`dataroastermysql.storage.storageClass` is mysql storage class which needs to be replaced with one of your kubernetes cluster.


You need to copy the randomly generated admin password which will be used to access dataroaster api.

```
kubectl logs -f $(kubectl get pod -l app=dataroaster-operator -o jsonpath="{.items[0].metadata.name}" -n dataroaster-operator) -n dataroaster-operator | grep "randomly generated password for user";
```

Output looks like this.
```
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
To access DataRoaster with REST, see [DataRoaster REST API](https://github.com/cloudcheflabs/dataroaster/tree/master/operators/dataroaster) for more details.



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
