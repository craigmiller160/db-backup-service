#!/bin/sh

mvn clean compile exec:java \
  -Ddb.mongo.user=$MONGO_ROOT_USER \
  -Ddb.mongo.password=$MONGO_ROOT_PASSWORD \
  -Ddb.postgres.user= \
  -Ddb.postgres.password=