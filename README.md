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

## Deploying to Production

First, double check the `deploy/deployment.yml` volume information. This should point to a path that represents an external hard drive. That way the data is being backed up in a place separate from the main OS drive, so that can be wiped safely without risking data loss.

Now, fully build the application with `mvn clean package`. Then, run the `deploy.sh` script to deploy it to Kubernetes in production.

## Adding Databases/Schemas to Backup

There is a file, `backup_config_prod.json`, in the root of the resources directory. Update this with the database/schema names and re-release to include it in the backup.