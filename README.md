# db-backup-service

An application to run and automatically backup data from databases in the Kubernetes environment.

## Overview

This application will run on a set interval and run `pg_dump` commands against the Postgres database, creating an archive of database backup files in a destination directory.

## Terraform Environment Variables

For the Terraform script to run, the following environment variables must be present on the machine.

```
# The operator access token for communicating with 1Password
ONEPASSWORD_TOKEN=XXXXXXX
```

## Setting Up Development Environment

This requires `pg_dump` to be available on the host system. To install it, run this command:

```
sudo apt install -y postgresql-client
```

This also requires `mongodump` to be available. The binary for it is in the `deploy/mongotools` directory for prod deployment. It can be installed from the web too, please make sure it is on the `PATH`.

## Running in Development

Use the `run.sh` script.

If the goal is to test the error alerts, the `email-service` must be running locally as well.

## Deploying to Production

First, an output directory needs to be configured on the local filesystem. The Kubernetes volume will point to `/home/craig/OtherDrive/DbBackup`.

Then, just use `craig-build`.

## Adding Databases/Schemas to Backup

There is a file, `backup_config_prod.json`, in the root of the resources directory. Update this with the database/schema names and re-release to include it in the backup.

## How To Restore Backup

### Postgres

First, you want to open a shell in the `db-backup-service` pod and navigate to the `/output` directory to find all the backups. This is because all the CLI tools for restoration are already installed here and Postgres is easily accessible.

Then, find the file you want and run this command:

```bash
psql -h postgres.infra-prod -U postgres_root {database name} -f {backup file}
```

### MongoDB

First, you want to open a shell in the `db-backup-service` pod and navigate to the `/output` directory to find all the backups. This is because all the CLI tools for restoration are already installed here and MongoDB is easily accessible.

Second, these backups are arranged as directories with a timestamp, not files with a timestamp. The whole directory is needed.

Now, just run the command on the backup you want:

```bash
mongorestore --uri="mongodb://mongodb.infra-prod:27017/admin?authSource=admin&tls=true" --username={username} --password={password} --tlsInsecure {direcotry}
```