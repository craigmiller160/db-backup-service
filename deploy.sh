#!/bin/bash

#
#     db-backup-service
#     Copyright (C) 2020 Craig Miller
#
#     This program is free software: you can redistribute it and/or modify
#     it under the terms of the GNU General Public License as published by
#     the Free Software Foundation, either version 3 of the License, or
#     (at your option) any later version.
#
#     This program is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU General Public License for more details.
#
#     You should have received a copy of the GNU General Public License
#     along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

raw_name=$(cat pom.xml | grep artifactId | head -n1 | tail -n1)
name=$(echo $raw_name | sed 's/^\s*<artifactId>//g' | sed 's/<\/artifactId>$//g')
raw_version=$(cat pom.xml | grep version | head -n2 | tail -n1)
version=$(echo $raw_version | sed 's/^\s*<version>//g' | sed 's/<\/version>$//g')
registry=localhost:32000
tag=$registry/$name:$version

check_artifact_version() {
  artifact_version=$(ls deploy/build | grep jar | sed 's/\.jar$//g' | sed "s/^$name-//g")
  if [ "$version" != "$artifact_version" ]; then
    echo "Project version $version does not equal artifact version $artifact_version"
    exit 1
  fi
}

check_deployment_version() {
  deployment_version=$(cat deploy/deployment.yml | grep $registry | sed "s/^.*$registry\/$name://g")
  if [ $version != $deployment_version ]; then
    echo "Project version $version does not equal deployment.yml version $deployment_version"
    exit 1
  fi
}

build() {
  echo "Building $name:$version"

  cd deploy
  sudo docker build \
    --network=host \
    -t $tag \
    .
  sudo docker push $tag

  sudo microk8s kubectl apply -f configmap.yml
  sudo microk8s kubectl apply -f deployment.yml
  sudo microk8s kubectl rollout restart deployment $name
}

check_artifact_version
check_deployment_version
build
