#!/bin/bash

set -eux;

USER=root
PASSWORD=anypass
SQL_PATH=/any/sqlpath

while getopts "U:P:S:" flag
do
         case "${flag}" in
                U) USER=${OPTARG};;
                P) PASSWORD=${OPTARG};;
                S) SQL_PATH=${OPTARG};;
         esac
done

echo "USER: $USER";
echo "PASSWORD: $PASSWORD";
echo "SQL_PATH: $SQL_PATH";


# create db schema.
java \
-cp dataroaster-operator-*.jar \
com.cloudcheflabs.dataroaster.operators.dataroaster.component.DBSchemaCreator ${USER} ${PASSWORD} ${SQL_PATH}



java \
-cp dataroaster-operator-*.jar \
-Ddataroaster.createDBSchema=true \
org.springframework.boot.loader.PropertiesLauncher ${USER} ${PASSWORD} ${SQL_PATH}
