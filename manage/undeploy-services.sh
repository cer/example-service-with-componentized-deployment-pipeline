#! /bin/bash -e

helm uninstall customer-service || echo uninstall failed

kubectl delete -k customer-service-deployment/k8s || echo nothing to delete
