#!/bin/bash -e

cd "$(dirname "$0")"

./test-cleanup.sh
./test-project-dependency-checks-do-not-compile.sh
./test-generated-service-is-checked.sh
