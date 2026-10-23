#!/bin/bash -e

# checkProjectDependencies and checkSubdomainGraph read declared dependencies only, so running them must not compile anything.
# A dry run lists each scheduled task as "<path> SKIPPED"; buildSrc's own compilation is not in that list.

cd "$(dirname "$0")/.."

output=$(./gradlew checkProjectDependencies checkSubdomainGraph --dry-run --console=plain)

if ! echo "$output" | grep -q '^:customer-management:customer-management-domain:checkProjectDependencies SKIPPED$' ; then
  echo "FAIL: checkProjectDependencies was not scheduled for customer-management-domain"
  echo "$output"
  exit 1
fi

if ! echo "$output" | grep -q '^:checkSubdomainGraph SKIPPED$' ; then
  echo "FAIL: checkSubdomainGraph was not scheduled for the root project"
  echo "$output"
  exit 1
fi

if echo "$output" | grep -q ':compileJava SKIPPED$' ; then
  echo "FAIL: checkProjectDependencies or checkSubdomainGraph scheduled a compileJava task"
  echo "$output"
  exit 1
fi

echo "PASS: checkProjectDependencies and checkSubdomainGraph run without compiling"
