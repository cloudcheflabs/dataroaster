#!/bin/bash

set -x

TEMP_VELERO={{ tempDirectory }};

# copy ceph credential to temp dir.
cp ceph-credentials-velero ${TEMP_VELERO}/;


cd ${TEMP_VELERO};
curl -L -O https://github.com/vmware-tanzu/velero/releases/download/v1.6.0/velero-v1.6.0-linux-amd64.tar.gz
tar zxvf velero-v1.6.0-linux-amd64.tar.gz
cd velero-v1.6.0-linux-amd64/
sudo cp velero /usr/local/bin/velero

# check velero.
velero

# move to temp dir.
cd ${TEMP_VELERO};

## define namespace
NAMESPACE={{ namespace }}

# install velero with restic.
velero install \
--provider aws \
--plugins velero/velero-plugin-for-aws:v1.0.0 \
--bucket {{ s3Bucket }} \
--secret-file ./ceph-credentials-velero \
--backup-location-config region=ceph \
--backup-location-config s3ForcePathStyle="true" \
--backup-location-config s3Url={{ s3Endpoint }} \
--snapshot-location-config region=ceph \
--use-restic \
--namespace ${NAMESPACE} \
--kubeconfig={{ kubeconfig }};

