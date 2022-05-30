#!/bin/bash

set -x

# create temp dir.
TEMP_VELERO=/tmp/velero;
mkdir -p ${TEMP_VELERO};

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


# install velero with restic.
velero install \
--provider aws \
--plugins velero/velero-plugin-for-aws:v1.0.0 \
--bucket velero-backups \
--secret-file ./ceph-credentials-velero \
--backup-location-config region=ceph \
--backup-location-config s3ForcePathStyle="true" \
--backup-location-config s3Url=https://ceph-rgw-test.cloudchef-labs.com \
--snapshot-location-config region=ceph \
--use-restic


# remove temp dir.
rm -rf ${TEMP_VELERO};

