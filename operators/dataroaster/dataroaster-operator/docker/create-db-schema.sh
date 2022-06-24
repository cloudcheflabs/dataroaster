#!/bin/bash

set -eux;

HOST=localhost
USER=root
PASSWORD=anypass
SQL_PATH=/any/sqlpath

while getopts "H:U:P:S:" flag
do
         case "${flag}" in
                H) HOST=${OPTARG};;
                U) USER=${OPTARG};;
                P) PASSWORD=${OPTARG};;
                S) SQL_PATH=${OPTARG};;
         esac
done
echo "HOST: $HOST";
echo "USER: $USER";
echo "PASSWORD: $PASSWORD";
echo "SQL_PATH: $SQL_PATH";


# create db schema.
java \
-cp dataroaster-operator-*.jar \
com.cloudcheflabs.dataroaster.operators.dataroaster.component.DBSchemaCreator ${HOST} ${USER} ${PASSWORD} ${SQL_PATH}
