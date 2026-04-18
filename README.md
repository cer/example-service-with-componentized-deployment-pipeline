# Example Service

This project is a Spring Boot-based customer service application that primarily serves as a demonstration of the [github-build-and-test-action](https://github.com/cer/github-build-and-test-action/tree/main) custom GitHub Action. The CI workflow in this repository illustrates the kind of build, test, and deployment pipeline that the action is designed to support.

## What this project demonstrates

The [CI workflow](.github/workflows/ci.yaml) exercises a realistic end-to-end pipeline:

1. **Build** - Compiles a multi-module Gradle/Java 17 project using Spring Boot
2. **Validate Kubernetes manifests** - Runs kubeconform against K8s YAML and Helm charts
3. **Create a Kind cluster** - Spins up a local Kubernetes cluster in CI
4. **Deploy infrastructure** - Installs infrastructure services (Postgres, Kafka, authorization server) via Helm
5. **End-to-end testing** - Deploys the application into the cluster and runs integration tests against it
6. **Artifact collection** - Uploads test reports and container logs as build artifacts

## Application overview

The application itself is a customer service built with the [Eventuate](https://eventuate.io/) platform. It exposes a REST API for managing customers, uses PostgreSQL for persistence, Kafka for messaging, and OAuth2/JWT for security.

### Project modules

| Module | Purpose |
|---|---|
| `customer-service-api-web` | REST API request/response types |
| `customer-service-api-messaging` | Messaging API types |
| `customer-service-domain` | Domain model and business logic |
| `customer-service-persistence` | Database persistence |
| `customer-service-web` | REST controllers and security configuration |
| `customer-service-messaging` | Message handlers |
| `customer-service-main` | Spring Boot application entry point and Dockerfile |

### Kubernetes deployment

The `customer-service-deployment/` directory contains:

- **`k8s/`** - Plain Kubernetes manifests (Deployment, Service, Kustomize overlays)
- **`helm-charts/customer-service/`** - A Helm chart with templates for deployment, service, ingress, HPA, canary releases, and Flux image update automation

## Prerequisites

- Java 17
- Docker (for Kind cluster and container images)
- [Kind](https://kind.sigs.k8s.io/) (for local Kubernetes cluster)
- [Helm](https://helm.sh/) (for deploying to Kubernetes)
- [Kubeconform](https://github.com/yannh/kubeconform)

## Building

```sh
./gradlew build
```

## Running tests

```sh
./test/test-end-to-end.sh
```

This script:

1. Validates the Helm chart YAML
2. Runs the Gradle build
3. Creates a Kind cluster
4. Deploys infrastructure services via Helm
5. Deploys the service using Helm
6. Runs smoke tests against the deployed service

## License

Apache License 2.0 - see [LICENSE.md](LICENSE.md).
