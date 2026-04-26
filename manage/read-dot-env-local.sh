# shellcheck disable=SC2148
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="${DIR}/.."

if [ -f "$PROJECT_ROOT/.env.local" ]; then
  set -o allexport
  # shellcheck disable=SC1091
  source "$PROJECT_ROOT/.env.local"
  set +o allexport
fi
