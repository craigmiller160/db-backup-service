data "keycloak_realm" "apps_dev" {
  realm = "apps-dev"
}

data "keycloak_realm" "apps_prod" {
  realm = "apps-prod"
}

locals {
  client_common = {
    client_id = "db-backup-service"
    name = "db-backup-service"
    enabled = true
    access_type = "CONFIDENTIAL"
    service_accounts_enabled = true
  }

  access_role_common = {
    name = "access"
  }
}

resource "keycloak_openid_client" "db_backup_service_dev" {
  realm_id = data.keycloak_realm.apps_dev.id
  client_id = local.client_common.client_id
  name = local.client_common.name
  enabled = local.client_common.enabled
  access_type = local.client_common.access_type
  service_accounts_enabled = local.client_common.service_accounts_enabled
}

resource "keycloak_openid_client" "db_backup_service_prod" {
  realm_id = data.keycloak_realm.apps_prod.id
  client_id = local.client_common.client_id
  name = local.client_common.name
  enabled = local.client_common.enabled
  access_type = local.client_common.access_type
  service_accounts_enabled = local.client_common.service_accounts_enabled
}

resource "keycloak_role" "db_backup_service_access_role_dev" {
  realm_id = data.keycloak_realm.apps_dev.id
  client_id = keycloak_openid_client.db_backup_service_dev.id
  name = local.access_role_common.name
}

resource "keycloak_role" "db_backup_service_access_role_prod" {
  realm_id = data.keycloak_realm.apps_prod.id
  client_id = keycloak_openid_client.db_backup_service_prod.id
  name = local.access_role_common.name
}

data "keycloak_openid_client" "email_service_client_dev" {
  count = var.email_service_available
  realm_id = data.keycloak_realm.apps_dev.id
  client_id = "email-service"
}

data "keycloak_openid_client" "email_service_client_prod" {
  count = var.email_service_available
  realm_id = data.keycloak_realm.apps_prod.id
  client_id = "email-service"
}

data "keycloak_role" "email_service_access_role_dev" {
  count = var.email_service_available
  realm_id = data.keycloak_realm.apps_dev.id
  client_id = data.keycloak_openid_client.email_service_client_dev[0].id
  name = local.access_role_common.name
}

data "keycloak_role" "email_service_access_role_prod" {
  count = var.email_service_available
  realm_id = data.keycloak_realm.apps_prod.id
  client_id = data.keycloak_openid_client.email_service_client_prod[0].id
  name = local.access_role_common.name
}

resource "keycloak_openid_client_service_account_role" "db_backup_service_email_service_access_dev" {
  count = var.email_service_available
  realm_id = data.keycloak_realm.apps_dev.id
  service_account_user_id = keycloak_openid_client.db_backup_service_dev.service_account_user_id
  client_id = data.keycloak_openid_client.email_service_client_dev[0].id
  role = local.access_role_common.name
}

resource "keycloak_openid_client_service_account_role" "db_backup_service_email_service_access_prod" {
  count = var.email_service_available
  realm_id = data.keycloak_realm.apps_prod.id
  service_account_user_id = keycloak_openid_client.db_backup_service_prod.service_account_user_id
  client_id = data.keycloak_openid_client.email_service_client_prod[0].id
  role = local.access_role_common.name
}