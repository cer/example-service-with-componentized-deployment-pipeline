#!/bin/bash -e

# A service generated from the template, with a second subdomain added, must run all three check tasks in every project and pass.

cd "$(dirname "$0")/.."

GENERATED=build/generated-service-test

copy_template() {
  mkdir -p "$GENERATED"
  rsync -a --delete --exclude .git --exclude .gradle --exclude build ./ "$GENERATED/"
}

add_billing_subdomain() {
  local project_dir="$GENERATED/billing/billing-domain"
  local package_dir="$project_dir/src/main/java/io/eventuate/customerservice/billing/domain"

  mkdir -p "$package_dir"

  cat > "$project_dir/build.gradle" <<'EOF'
plugins {
    id 'io.eventuate.architecture.roles.domain'
}
EOF

  cat > "$package_dir/Invoice.java" <<'EOF'
package io.eventuate.customerservice.billing.domain;

public class Invoice {
}
EOF

  echo "include 'billing:billing-domain'" >> "$GENERATED/settings.gradle"
}

assert_task_executed() {
  local task=$1
  if ! echo "$output" | grep -q "^> Task $task\( \|$\)" ; then
    echo "FAIL: $task was not executed"
    echo "$output"
    exit 1
  fi
}

copy_template
add_billing_subdomain

if ! output=$(cd "$GENERATED" && ./gradlew checkProjectDependencies checkSubdomainGraph checkArchitectureRules --console=plain) ; then
  echo "FAIL: the check tasks failed in the generated service"
  echo "$output"
  exit 1
fi

projects=(:billing:billing-domain)
for dir in "$GENERATED"/customer-management/*/ ; do
  projects+=(":customer-management:$(basename "$dir")")
done

for project in "${projects[@]}" ; do
  assert_task_executed "$project:checkProjectDependencies"
  assert_task_executed "$project:checkArchitectureRules"
done

echo "PASS: the generated service runs the check tasks in every project and passes"
