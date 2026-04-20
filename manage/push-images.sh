#! /bin/bash -e

VERSION=${1:-0.1.0-SNAPSHOT}

if [ -n "$GITHUB_TOKEN" ]; then
    echo logging into ghcr.io
    echo "${GITHUB_TOKEN?}" | docker login ghcr.io -u "${GITHUB_REPOSITORY_OWNER?}" --password-stdin
    echo logged into ghcr.io
else
    echo GITHUB_TOKEN is not set - not logging in
fi

./gradlew --build-cache -P imageVersion="${VERSION}" \
    -P "imageRemoteRegistry=ghcr.io/${GITHUB_REPOSITORY?}"  \
    buildDockerImageRemote

