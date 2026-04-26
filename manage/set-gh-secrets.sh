#!/bin/bash -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# shellcheck disable=SC1091
source "$DIR/read-dot-env-local.sh"

gh secret set GH_TOKEN --body "${GITHUB_TOKEN?}"
