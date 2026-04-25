#! /bin/bash -e

NO_BUILD=
WITH_AUTH=
PRIVATE_REGISTRY=

release_name=
helm_install_args=()

while [[ "$1" == --* ]] ; do
  case $1 in
    "--no-build" )
      NO_BUILD=yes
      ;;
    "--release-name" )
      release_name=${2?}
      shift
      ;;
    "--with-auth" )
      WITH_AUTH=yes
      ;;
    "--private-registry" )
      NO_BUILD=yes
      PRIVATE_REGISTRY=yes
      ;;
    "--chart-version" )
      helm_install_args=("--version" "${2?}")
      shift
      ;;
    --*)
      echo bad option "$1" - ./test/test-service-chart.sh '[--no-build]' '[--with-auth]' '[--release-name]' 'service-name' '[ingress-test-path]'
      exit 1
      ;;
  esac
  shift
done

service_name=${1?}    
ingress_test_path=$2

if [ -z "$release_name" ] ; then
    release_name="$service_name"
fi

gradle_build() {
    if [ -z "$NO_BUILD" ] ; then
        ./gradlew -P imageVersion=0.1.0-SNAPSHOT buildDockerImageLocally
    else
        echo skipping build
    fi
}

gradle_build

echo installing "$service_name"

if [ -n "$PRIVATE_REGISTRY" ] ; then
  helmOpts=()
  chart="oci://ghcr.io/cer/example-service-with-componentized-deployment-pipeline/charts/${service_name?}"
else
  helmOpts=("--set-string" "image.repository=localhost:5002/${service_name}")
  chart="$service_name-deployment/helm-charts/$service_name"
  helm dependency update "$chart"
fi

if [ -n "$ingress_test_path" ] ; then
  helmOpts+=("--set" "ingress.enabled=true" "--set" "ingress.className=nginx" "--set" "ingress.hosts[0].host=" "--set" "ingress.hosts[0].paths[0].path=${ingress_test_path}" "--set" "ingress.hosts[0].paths[0].pathType=Prefix")
fi

helm upgrade --install "$release_name" "$chart"  "${helm_install_args[@]}"  "${helmOpts[@]}" --wait

kubectl rollout status deployment "$release_name" --timeout=90s

echo running helm test "$release_name" ...

SUCCESS=

for _i in {1..5}; do
    if helm test "$release_name" ; then
        SUCCESS=yes
        break
    fi
    echo retrying
    sleep 1
done

# Don't test again if previously successful

if [ -z "$SUCCESS" ] ; then
    helm test "$release_name"
fi

# At this point the service is ready
#

echo 

if [ -n "$ingress_test_path" ] ; then

    if [ -n "$WITH_AUTH" ] ; then
      POD_NAME=$(kubectl get pods --namespace default -l "app.kubernetes.io/name=authorization-server,app.kubernetes.io/instance=authorization-server" --field-selector=status.phase=Running -o jsonpath="{.items[0].metadata.name}")
      JWT=$(kubectl exec "$POD_NAME" -- curl -s -X POST -u messaging-client:secret -d "client_id=messaging-client" -d "username=user" -d "password=password" \
          -d "grant_type=password" \
          http://localhost:9000/oauth2/token | jq -r .access_token)
      authOpts=("-H" "Authorization: Bearer $JWT")
    else
      authOpts=()
    fi

    echo accessing ingress "$ingress_test_path" with authOpts "${authOpts[@]}"

    for _i in {1..5}; do
        if curl "${authOpts[@]}" --retry-connrefused --retry 5 --retry-delay 1 --fail "localhost$ingress_test_path" > /dev/null ; then
            break
        fi
        echo retrying
        sleep 1
    done

    curl "${authOpts[@]}" --retry-connrefused --retry 5 --retry-delay 1 --fail "localhost${ingress_test_path?}"

else
    echo skipping ingress path check
fi
echo 


echo SUCCESS

