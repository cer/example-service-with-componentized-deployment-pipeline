#! /bin/bash -e

PORT=8888

echo "Getting JWT from authorization-server..."

JWT=$(./test/get-jwt.sh)

echo "Port-forwarding customer-service on localhost:${PORT}..."

kubectl port-forward deployment/customer-service ${PORT}:8080 &
PF_PID=$!

trap 'kill $PF_PID 2>/dev/null' EXIT

sleep 2

echo "Testing /actuator/health endpoint..."

curl --fail --retry 3 --retry-delay 1 --retry-connrefused \
  "http://localhost:${PORT}/actuator/health"

echo

echo "Testing /customers endpoint..."

curl --fail --retry 3 --retry-delay 1 --retry-connrefused \
  -H "Authorization: Bearer ${JWT}" \
  "http://localhost:${PORT}/customers"

echo

echo "Creating a customer..."

curl --fail \
  -H "Authorization: Bearer ${JWT}" \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "creditLimit": {"amount": 1000}}' \
  "http://localhost:${PORT}/customers"

echo

echo "Verifying customer was created..."

curl --fail \
  -H "Authorization: Bearer ${JWT}" \
  "http://localhost:${PORT}/customers"

echo
echo "SUCCESS"
