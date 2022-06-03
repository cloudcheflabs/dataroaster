#!/bin/bash

# add helm repo.
helm repo add jupyterhub https://jupyterhub.github.io/helm-chart/

## define namespace
NAMESPACE=dataroaster-jupyterhub

## define helm application name.
APP_NAME=jupyterhub


# create config.
cat <<EOF > dataroaster-values.yaml
hub:
  config:
    GitHubOAuthenticator:
      client_id: "0b322767446baedb3203"
      client_secret: "828688ff8be545b6434df2dbb2860a1160ae1517"
      oauth_callback_url: "https://jupyterhub-test.cloudchef-labs.com/hub/oauth_callback"
  db:
    pvc:
      storageClassName: ceph-rbd-sc
proxy:
  secretToken: $(openssl rand -hex 32)
singleuser:
  image:
    name: cloudcheflabs/dataroaster-jupyter
    tag: '1.1.3'
    pullPolicy: Always
  storage:
    capacity: 1Gi
    dynamic:
      storageClass: ceph-rbd-sc
ingress:
  enabled: true
  annotations:
    kubernetes.io/ingress.class: nginx
    kubernetes.io/tls-acme: "true"
    cert-manager.io/cluster-issuer: letsencrypt-prod
  hosts:
    - jupyterhub-test.cloudchef-labs.com
  pathSuffix:
  pathType: Prefix
  tls:
    - hosts:
      - jupyterhub-test.cloudchef-labs.com
      secretName: jupyterhub-test.cloudchef-labs.com-tls
EOF



echo "values.yaml: "
cat dataroaster-values.yaml

# install jupyterhub.
helm upgrade --cleanup-on-fail \
--install $APP_NAME \
jupyterhub/jupyterhub \
--namespace $NAMESPACE \
--create-namespace \
--version=1.1.3 \
--values dataroaster-values.yaml;


# wait for a while to initialize jupyterhub.
sleep 30

# wait for jupyterhub being run.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app=jupyterhub,component=hub \
  --timeout=120s
