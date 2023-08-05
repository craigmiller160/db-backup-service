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

import "keycloak_openid_client.db_backup_service_dev" "apps-dev/"
import "keycloak_openid_client.db_backup_service_prod" "apps-prod/"

# TODO not done yet

plan
