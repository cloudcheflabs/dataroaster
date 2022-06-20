# DataRoaster Helm Operator

DataRoaster Helm Operator is used to install / upgrade / uninstall applications of helm charts easily.

## Install DataRoaster Helm Operator

```
helm repo add dataroaster-helm-operator https://cloudcheflabs.github.io/helm-operator-helm-repo/
helm repo update

helm install \
helm-operator \
--create-namespace \
--namespace helm-operator \
--version v1.0.0 \
dataroaster-helm-operator/dataroaster-helm-operator;
```

Check if helm operator is running.

```
kubectl get po -n helm-operator;
NAME                             READY   STATUS    RESTARTS   AGE
helm-operator-5464cdc75f-fw9zn   1/1     Running   0          10m
```


## Install Helm Chart Applications using Custom Resources

For instance, let's create the custom resource of ingress controller nginx helm chart.

```

```