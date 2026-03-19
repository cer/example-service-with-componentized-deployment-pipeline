#! /bin/bash -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR/.."

echo "=== Validate K8S manifests ==="

./test/validate-k8s-yaml.sh

echo "=== Build ==="

./gradlew build

echo "=== Create Kind Cluster ==="

./manage/create-kind-cluster.sh

echo "=== Test Kind installation ==="

./test/test-kind-cluster.sh

echo "=== Install infrastructure services ==="

./manage/install-infrastructure-services.sh

echo "=== Test Kafka ==="

./test/test-kafka.sh

echo "=== Test Get JWT ==="

./test/get-jwt.sh

echo "=== Test customer service ==="

./test/test-all-services.sh

echo
echo "=== ALL TESTS PASSED ==="
