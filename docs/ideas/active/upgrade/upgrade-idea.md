# Upgrades: Align with spring-boot-eventuate Template

Upgrades needed to bring example-service in line with the spring-boot-eventuate service template.

**Skills**: Use the `eventuate-development` plugin skills as the authoritative reference for each task.

## Testing

- [ ] Migrate from JUnit 4 to JUnit 5 — replace `junit:junit:4.13.2` / `4.12` with JUnit Platform and `useJUnitPlatform()` globally (skill: `eventuate-development:eventuate-gradle-setup`)
- [ ] Add API docs generation test in `customer-service-main/src/test` (skill: `eventuate-development:service-main`)
- [ ] Add `eventuate-tram-spring-testing-support-outbox` and `eventuate-tram-spring-testing-support-producer-kafka` to component test dependencies (skill: `eventuate-development:service-main`)
- [ ] Remove disabled Spring Cloud Contract configuration (vestigial SB2-only code in `customer-service-web` and `customer-service-messaging`)
- [ ] Use `org.testcontainers:junit-jupiter` for TestContainers integration (JUnit 5 lifecycle) (skill: `eventuate-development:persistence-adapter`)

## Build Configuration

- [ ] Align root `build.gradle` with the Eventuate Gradle setup pattern (skill: `eventuate-development:eventuate-gradle-setup`):
  - Add `-parameters` compiler flag
  - Add `testRuntimeOnly 'org.junit.platform:junit-platform-launcher'` globally
  - Remove direct `junit:junit` dependency from root and subproject `build.gradle` files
  - Add `useJUnitPlatform()` globally

## Dependencies

- [ ] Add Springwolf support (skill: `eventuate-development:service-main`)
- [ ] Upgrade `eventuate-platform-dependencies` from 2025.1.BUILD-SNAPSHOT to 2026.1.BUILD-SNAPSHOT (skill: `eventuate-development:eventuate-gradle-setup`)

## Module Structure

- [ ] Inline `customer-service-api-web` and `customer-service-api-messaging` — copy their classes into the projects that depend on them, then remove both modules from `settings.gradle`
- [ ] Reorganize flat subprojects into nested `customer-management/customer-management-*` structure and rename (skill: `eventuate-development:eventuate-module-structure`):
  - `customer-service-domain` -> `customer-management:customer-management-domain`
  - `customer-service-web` -> `customer-management:customer-management-web-api`
  - `customer-service-messaging` -> `customer-management:customer-management-command-api`
  - `customer-service-persistence` -> `customer-management:customer-management-persistence`

## Domain Event Publishing

- [ ] Add `CustomerCreditReservedEvent` (skill: `eventuate-development:domain-event-publishing`):
  - Add `eventuate-tram-spring-events` dependency to domain module
  - Create `CustomerEvent` interface extending `DomainEvent` in domain
  - Create `CustomerCreditReservedEvent` record implementing `CustomerEvent`
  - Create `CustomerEventPublisher` interface extending `DomainEventPublisherForAggregate<Customer, Long, CustomerEvent>` in domain
  - Create `CustomerEventPublisherImpl` extending `AbstractDomainEventPublisherForAggregateImpl` in event-publishing module
  - Inject `CustomerEventPublisher` into `CustomerService` and publish after `reserveCredit`

## Domain Event Subscription

- [ ] Add a `CustomerCreditReservedEventConsumer` (skill: `eventuate-development:domain-event-subscription`):
  - Create event consumer class with `@EventuateDomainEventHandler` annotation listening on the `Customer` aggregate channel
  - Create `CustomerEventSubscribersConfiguration` to wire the consumer bean
  - Add `eventuate-tram-spring-events-subscriber-starter` dependency
  - Add unit test using `TramInMemoryConfiguration` + H2 + `@MockitoBean` + Awaitility
  - Add integration test using TestContainers (PostgreSQL + Kafka)

## Cleanup

- [ ] Remove `spring-cloud-contract` buildscript dependency and conditional blocks guarded by `!springBootVersion.startsWith("3")`
- [ ] Remove `maven-publish` plugin and stub publication config from `customer-service-web` and `customer-service-messaging` (tied to contract testing)
- [ ] Remove outdated Mockito 2.23.0 pinned dependency in `customer-service-persistence` (use version from Spring Boot BOM)
