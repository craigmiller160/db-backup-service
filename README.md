# db-backup-service

An application to run and automatically backup data from databases in the Kubernetes environment.

## Setting Up Development Environment

This requires `pg_dump` to be available on the host system. To install it, run this command:

```
sudo apt install -y postgresql-client
```