#!/bin/sh

function import {
  terraform \
    import \
    -var="onepassword_token=$ONEPASSWORD_TOKEN"\
    "$1" "$2"
}

function plan {
  terraform plan \
    -var "onepassword_token=$ONEPASSWORD_TOKEN"
}

import "keycloak_openid_client.db_backup_service_dev" "apps-dev/a2d1def0-5c76-4e30-beb4-75958ad706f0"
import "keycloak_openid_client.db_backup_service_prod" "apps-prod/1d9760b9-1cdb-4a49-9ff9-fac415b1a52c"

import "keycloak_role.db_backup_service_access_role_dev" "apps-dev/51b678ed-9455-450a-b524-baedc2c95324"
import "keycloak_role.db_backup_service_access_role_prod" "apps-prod/6bc4427f-6c4f-4ea4-9f4f-7fac0b4a3f06"

import "keycloak_openid_client_service_account_role.db_backup_service_email_service_access_dev" "apps-dev/"
import "keycloak_openid_client_service_account_role.db_backup_service_email_service_access_prod" "apps-prod/"

# TODO not done yet

plan
