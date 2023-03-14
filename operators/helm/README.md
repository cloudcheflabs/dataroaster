# DataRoaster Helm Operator

DataRoaster Helm Operator is used to install / upgrade / uninstall applications of helm charts easily.

## Install DataRoaster Helm Operator

```
helm repo add dataroaster-helm-operator https://cloudcheflabs.github.io/helm-operator-helm-repo/
helm repo update

helm install \
helm-operator \
--create-namespace \
--namespace dataroaster-operator \
--version v3.0.0 \
dataroaster-helm-operator/dataroasterhelmoperator;
```

Check if helm operator is running.

```
kubectl get po -n helm-operator;
NAME                             READY   STATUS    RESTARTS   AGE
helm-operator-5464cdc75f-fw9zn   1/1     Running   0          10m
```


## Install Helm Chart Applications using Custom Resources

For instance, let's create the custom resource `helm-nginx.yaml` of ingress controller nginx helm chart.

```
apiVersion: "helm-operator.cloudchef-labs.com/v1beta1"
kind: HelmChart
metadata:
  name: ingress-nginx
  namespace: helm-operator
spec:
  repo: https://kubernetes.github.io/ingress-nginx
  chartName: ingress-nginx
  name: ingress-nginx
  version: 4.0.17
  namespace: ingress-nginx
  values: |
    replicaCount: 1
    minAvailable: 1
```

Create nginx ingress controller using this custom resource.
```
kubectl apply -f helm-nginx.yaml;
```

Let's see if ingress nginx is running.

```
kubectl get po -n ingress-nginx
NAME                                        READY   STATUS    RESTARTS   AGE
ingress-nginx-controller-7445b7d6dc-plvw9   1/1     Running   0          24s
```

Custom resource `helmcharts` has been created like this.

```
kubectl get helmcharts -n helm-operator
NAME            AGE
ingress-nginx   2m3s
```

To upgrade nginx chart, `helmcharts` custom resource `ingress-nginx` needs to be edited or modified custom resource yaml file needs to be applied.
```
kubectl edit helmcharts ingress-nginx -n helm-operator;
...
apiVersion: helm-operator.cloudchef-labs.com/v1beta1
kind: HelmChart
metadata:
  annotations:
    kubectl.kubernetes.io/last-applied-configuration: |
      {"apiVersion":"helm-operator.cloudchef-labs.com/v1beta1","kind":"HelmChart","metadata":{"annotations":{},"name":"ingress-nginx","namespace":"helm-operator"},"spec":{"chartName":"ingress-nginx","name":"ingress-nginx","namespace":"ingress-nginx","repo":"https://kubernetes.github.io/ingress-nginx","values":"replicaCount: 1\nminAvailable: 1\n","version":"4.0.17"}}
  creationTimestamp: "2022-06-20T13:43:32Z"
  generation: 1
  name: ingress-nginx
  namespace: helm-operator
  resourceVersion: "11697"
  uid: 3f7a9a2b-24ee-4a38-9e7c-09832f78e01c
spec:
  chartName: ingress-nginx
  name: ingress-nginx
  namespace: ingress-nginx
  repo: https://kubernetes.github.io/ingress-nginx
  values: |
    replicaCount: 1
    minAvailable: 1
  version: 4.0.17
...
```
Change the version of `spec.version` to `4.1.4`, for instance, then ingress controller nginx chart will be upgraded to `4.1.4`.

To delete nginx chart.
```
kubectl delete -f helm-nginx.yaml;
```
