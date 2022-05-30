#!/bin/bash

# add helm repo.
helm repo add jupyterhub https://jupyterhub.github.io/helm-chart/

## define namespace
NAMESPACE={{ jupyterhubNamespace }}

## define helm application name.
APP_NAME=jupyterhub


# create config.
cat <<EOF > dataroaster-values.yaml
hub:
  config:
    GitHubOAuthenticator:
      client_id: "{{ jupyterhubGithubClientId }}"
      client_secret: "{{ jupyterhubGithubClientSecret }}"
      oauth_callback_url: "https://{{ jupyterhubIngressHost }}/hub/oauth_callback"
  db:
    pvc:
      storageClassName: {{ storageClass }}
proxy:
  secretToken: $(openssl rand -hex 32)
singleuser:
  image:
    name: cloudcheflabs/dataroaster-jupyter
    tag: '1.1.3'
    pullPolicy: Always
  storage:
    capacity: {{ jupyterhubStorageSize }}Gi
    dynamic:
      storageClass: {{ storageClass }}
ingress:
  enabled: true
  annotations:
    kubernetes.io/ingress.class: nginx
    kubernetes.io/tls-acme: "true"
    cert-manager.io/cluster-issuer: letsencrypt-prod
  hosts:
    - {{ jupyterhubIngressHost }}
  pathSuffix:
  pathType: Prefix
  tls:
    - hosts:
      - {{ jupyterhubIngressHost }}
      secretName: {{ jupyterhubIngressHost }}-tls
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
--values dataroaster-values.yaml \
--kubeconfig={{ kubeconfig }};


# wait for a while to initialize jupyterhub.
sleep 30

# wait for jupyterhub being run.
kubectl wait --namespace ${NAMESPACE} \
--for=condition=ready pod \
--selector=app=jupyterhub,component=hub \
--timeout=120s \
--kubeconfig={{ kubeconfig }};
