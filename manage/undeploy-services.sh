#! /bin/bash -e

helm uninstall customer-service || echo uninstall failed
