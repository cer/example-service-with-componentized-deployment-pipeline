#! /bin/bash -e

service_name=${1?}
version=${2?}
app_version=${3:-$version}

chart_dir=${service_name}-deployment/helm-charts/$service_name

helm package --dependency-update --version "$version" --app-version "$app_version" "$chart_dir" -d helm-repository

echo "${GITHUB_TOKEN?}" | helm registry login --username "${GITHUB_REPOSITORY_OWNER?}" --password-stdin ghcr.io

helm push "helm-repository/${service_name?}-$version.tgz" \
    "oci://ghcr.io/${GITHUB_REPOSITORY?}/charts"

echo published "$version"
