# Example Service

This project is a template for [Eventuate](https://eventuate.io/)-based Spring Boot services deployed to Kubernetes as Helm charts. It implements a `Customer Service` that demonstrates the architecture, testing strategy, and CI/CD pipeline intended for production Eventuate services.

## What this project demonstrates

- **Service architecture** — hexagonal (ports and adapters) subdomain structure with Eventuate Tram for messaging, sagas, and domain events
- **Testing strategy** — unit, integration, component, and end-to-end tests across all architectural layers
- **Kubernetes deployment** — Helm charts for deploying the service and its infrastructure
- **CI/CD pipeline** — the [CI workflow](.github/workflows/ci.yaml), built using the [github-build-and-test-action](https://github.com/cer/github-build-and-test-action/tree/main) custom GitHub Action, exercises a realistic end-to-end pipeline:
  1. **Build** — Compiles a multi-module Gradle/Java 17 project using Spring Boot
  2. **Validate Kubernetes manifests** — Runs kubeconform against Helm charts
  3. **Create a Kind cluster** — Spins up a local Kubernetes cluster in CI
  4. **Deploy infrastructure** — Installs infrastructure services (Postgres, Kafka, authorization server) via Helm
  5. **End-to-end testing** — Deploys the application into the cluster and runs integration tests against it
  6. **Artifact collection** — Uploads test reports and container logs as build artifacts

## Application overview

The application itself is a customer service built with the [Eventuate](https://eventuate.io/) platform. It exposes a REST API for managing customers, uses PostgreSQL for persistence, Kafka for messaging, and OAuth2/JWT for security. It uses the Eventuate Tram Saga framework to orchestrate credit reservation via a saga with local and participant steps.

### Service Architecture

The service is built on the [Eventuate](https://eventuate.io/) platform and consists of one or more subdomains plus a service main module. Each subdomain follows a hexagonal (ports and adapters) architecture with a domain at the center and adapters for external concerns. This service has a single subdomain — **customer-management**.

#### Customer management subdomain

**Domain modules:**

| Module | Purpose |
|---|---|
| `customer-management/customer-management-domain` | Entity (`Customer`), domain service, repository interface, event classes |
| `customer-management/customer-management-sagas` | `ReserveCreditSaga` orchestration with local and participant steps |

**Adapters:**

| Module | Purpose |
|---|---|
| `customer-management/customer-management-web-api` | REST controllers, security configuration, request/response DTOs |
| `customer-management/customer-management-persistence` | JPA configuration, Flyway migrations |
| `customer-management/customer-management-event-publishing` | Domain event publisher implementation |
| `customer-management/customer-management-event-subscribers` | Domain event consumers |
| `customer-management/customer-management-command-api` | Saga participant command handler (`ReserveCreditCommand`) |

#### Service main

| Module | Purpose |
|---|---|
| `customer-service-main` | Spring Boot application entry point, aggregates all subdomain modules |

### Technology stack

- **Language and runtime:** Java 17, Spring Boot 3.4
- **Persistence:** JPA/Hibernate with PostgreSQL, Flyway migrations, Spring Data JPA repositories
- **Messaging:** Eventuate Tram transactional messaging with Kafka (JDBC outbox pattern)
- **Sagas:** Eventuate Tram Sagas for orchestration-based credit reservation (`ReserveCreditSaga`)
- **Security:** OAuth2/JWT resource server
- **API documentation:** springdoc-openapi (REST) and Springwolf (Async API for Eventuate Tram events)

### Kubernetes deployment

The `customer-service-deployment/` directory contains:

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
