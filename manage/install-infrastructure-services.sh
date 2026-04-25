#! /bin/bash -e
# shellcheck disable=SC2086

helm repo add eventuate https://raw.githubusercontent.com/eventuate-platform/eventuate-helm-charts/helm-repository

helm repo update

helm upgrade --install kafka eventuate/kafka \
  --set image.tag=0.21.0.BUILD-SNAPSHOT \
  --set zookeeper.image.tag=0.21.0.BUILD-SNAPSHOT \
  $HELM_INFRASTRUCTURE_OPTS --wait

helm upgrade --install authorization-server eventuate/authorization-server \
  --set image.tag=0.2.0.BUILD-SNAPSHOT \
  --set users.initial[0].username=user \
  --set users.initial[0].password=password \
  --set users.initial[0].roles[0]=USER \
  --set users.initial[0].enabled=true \
  $HELM_INFRASTRUCTURE_OPTS --wait
