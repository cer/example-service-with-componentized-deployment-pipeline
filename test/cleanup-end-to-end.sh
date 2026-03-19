#! /bin/bash -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR/.."

echo "=== Dumping K8S state ==="

./.github/workflows/dump-k8s.sh || true

echo "=== Collecting container logs ==="

./.github/workflows/print-container-logs.sh || true

echo "=== Undeploying services ==="

./manage/undeploy-services.sh || true

echo "=== Deleting Kind cluster ==="

./manage/delete-kind-cluster.sh || true

echo
echo "=== Cleanup complete ==="
