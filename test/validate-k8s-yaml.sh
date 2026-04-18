#! /bin/bash -e

set -o pipefail

SRC_DIRS=(customer-service-deployment)

helm repo add eventuate https://raw.githubusercontent.com/eventuate-platform/eventuate-helm-charts/helm-repository

find "${SRC_DIRS[@]}" -name Chart.yaml | while read -r chart ; do
    echo "Validating $chart"
    CHART_DIR=$(dirname "$chart")

    rm -fr "$CHART_DIR/charts"

    helm dependency update "$CHART_DIR"

    helm template foo "$CHART_DIR" | kubeconform -verbose -strict
done
