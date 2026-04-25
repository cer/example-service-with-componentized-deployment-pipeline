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
  $HELM_INFRASTRUCTURE_OPTS --wait

kubectl set env deployment/authorization-server \
  SPRING_PROFILES_ACTIVE=UserDatabase \
  USERS_INITIAL_0_USERNAME=user \
  USERS_INITIAL_0_PASSWORD=password \
  "USERS_INITIAL_0_ROLES_0_=USER" \
  USERS_INITIAL_0_ENABLED=true

kubectl rollout status deployment/authorization-server --timeout=90s

# Wait for any terminating pods from the previous rollout to be fully removed
while [ "$(kubectl get pods -l 'app.kubernetes.io/name=authorization-server,app.kubernetes.io/instance=authorization-server' --no-headers | wc -l)" -gt 1 ]; do
  echo "Waiting for old authorization-server pods to terminate..."
  sleep 2
done
