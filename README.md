# db-backup-service

An application to run and automatically backup data from databases in the Kubernetes environment.

## Overview

This application will run on a set interval and run `pg_dump` commands against the Postgres database, creating an archive of database backup files in a destination directory.

## Setting Up Development Environment

This requires `pg_dump` to be available on the host system. To install it, run this command:

```
sudo apt install -y postgresql-client
```

## Running in Development

Use IntelliJ to run the `Runner.java` file.

If the goal is to test the error alerts, the application `email-service` must be running locally as well.

## Deploying to Production

First, an output directory needs to be configured on the local filesystem. The Kubernetes volume will point to `/opt/kubernetes/data/db-backup-service`, however that path should be symlinked so that it points to an external hard drive. That way the data is written to a place that will not be affected if the hard drive with the OS needs to be wiped/reinstalled.

Now, fully build the application with `mvn clean package`. Then, run the `deploy.sh` script to deploy it to Kubernetes in production.

## Adding Databases/Schemas to Backup

There is a file, `backup_config_prod.json`, in the root of the resources directory. Update this with the database/schema names and re-release to include it in the backup.