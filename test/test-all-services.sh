#! /bin/bash -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

"$DIR/test-service-chart.sh" "$@" --with-auth customer-service /customers /customers

printf "\nSUCCESS\n\n"
