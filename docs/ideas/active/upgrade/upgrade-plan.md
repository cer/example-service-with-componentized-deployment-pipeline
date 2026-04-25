## Idea Type: D — Modernization/alignment

# Upgrade — Implementation Plan

## Idea Type

**D. Modernization/alignment** — Align example-service with the spring-boot-eventuate service template: build configuration, module structure, domain event publishing/subscription, and testing patterns.

## Instructions for Coding Agent

- IMPORTANT: Use simple commands that you have permission to execute. Avoid complex commands that may fail due to permission issues.

### Required Skills

Use these skills by invoking them before the relevant action:

| Skill | When to Use |
|-------|-------------|
| `idea-to-code:plan-tracking` | ALWAYS - track task completion in the plan file |
| `idea-to-code:commit-guidelines` | Before creating any git commit |
| `idea-to-code:file-organization` | When moving, renaming, or reorganizing files |
| `idea-to-code:test-runner-java-gradle` | When running tests in Java/Gradle projects |
| `eventuate-development:eventuate-gradle-setup` | When modifying root build.gradle or gradle.properties |
| `eventuate-development:eventuate-module-structure` | When reorganizing modules or renaming packages |
| `eventuate-development:domain-event-publishing` | When implementing domain event publishing |
| `eventuate-development:domain-event-subscription` | When implementing domain event subscription |
| `eventuate-development:service-main` | When modifying customer-service-main (API docs, component tests) |
| `eventuate-development:persistence-adapter` | When modifying persistence integration tests |
| `eventuate-development:web-api-adapter` | When modifying web controller tests |
| `eventuate-development:saga-command-handler` | When modifying command handler configuration |

### Verification Requirements

- Hard rule: NEVER git commit unless you have successfully run `./gradlew compileAll` and it exits 0
- Before committing, run `./gradlew testAll` if tests exist for the changed modules

---

## Steel Thread 1: Build Configuration and Cleanup

Align root build.gradle with the Eventuate Gradle setup pattern and remove vestigial code. This thread has no functional dependencies and can be done first.

- [x] **Task 1.1: Align root build.gradle with Eventuate Gradle setup**
  - TaskType: OUTCOME
  - Entrypoint: `cat build.gradle`
  - Observable: Root build.gradle uses `useJUnitPlatform()`, `-parameters` flag, `junit-platform-launcher`, no `junit:junit` dependency
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [x] Add `useJUnitPlatform()` globally in subprojects block
    - [x] Add `-parameters` compiler flag via `tasks.withType(JavaCompile)`
    - [x] Add `testRuntimeOnly 'org.junit.platform:junit-platform-launcher'` globally
    - [x] Remove `junit:junit:4.13.2` dependency from root `build.gradle`
    - [x] Remove `junit:junit:4.12` dependency from `customer-service-persistence/build.gradle`

- [x] **Task 1.2: Upgrade eventuate-platform-dependencies**
  - TaskType: OUTCOME
  - Entrypoint: `cat gradle.properties`
  - Observable: `eventuatePlatformVersion` is `2026.1.BUILD-SNAPSHOT`
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [x] Update `eventuatePlatformVersion` in `gradle.properties` from `2025.1.BUILD-SNAPSHOT` to `2026.1.BUILD-SNAPSHOT`

- [x] **Task 1.3: Remove Spring Cloud Contract configuration**
  - TaskType: OUTCOME
  - Entrypoint: `grep -r spring-cloud-contract . --include='*.gradle' || echo not found`
  - Observable: No Spring Cloud Contract references remain in any build.gradle
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [x] Remove `spring-cloud-contract-gradle-plugin` buildscript dependency from root `build.gradle`
    - [x] Remove conditional `spring-cloud-contract` block from `customer-service-web/build.gradle`
    - [x] Remove conditional `spring-cloud-contract` block from `customer-service-messaging/build.gradle`

- [x] **Task 1.4: Remove maven-publish plugin and stub publication config**
  - TaskType: OUTCOME
  - Entrypoint: `grep -r maven-publish . --include='*.gradle' || echo not found`
  - Observable: No `maven-publish` references remain in web or messaging modules
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [x] Remove `maven-publish` plugin and publication config from `customer-service-web/build.gradle`
    - [x] Remove `maven-publish` plugin and publication config from `customer-service-messaging/build.gradle`

- [x] **Task 1.5: Remove outdated Mockito version pin**
  - TaskType: OUTCOME
  - Entrypoint: `cat customer-service-persistence/build.gradle`
  - Observable: No hardcoded Mockito version; uses Spring Boot BOM version
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [x] Remove `mockito-core:2.23.0` dependency from `customer-service-persistence/build.gradle`

---

## Steel Thread 2: Module Structure Reorganization

Inline the API modules and reorganize flat subprojects into nested `customer-management/customer-management-*` structure.

- [x] **Task 2.1: Inline customer-service-api-web into customer-service-web**
  - TaskType: OUTCOME
  - Entrypoint: `cat settings.gradle`
  - Observable: `customer-service-api-web` is no longer in `settings.gradle`; its classes are in `customer-service-web`
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [x] Copy classes from `customer-service-api-web/src/main/java` into `customer-service-web/src/main/java`
    - [x] Update `customer-service-web/build.gradle` to include any dependencies from `customer-service-api-web/build.gradle`
    - [x] Replace `project(":customer-service-api-web")` dependency in `customer-service-web/build.gradle` with the inlined dependencies
    - [x] Remove `customer-service-api-web` from `settings.gradle`
    - [x] Delete the `customer-service-api-web/` directory

- [x] **Task 2.2: Inline customer-service-api-messaging into customer-service-messaging**
  - TaskType: OUTCOME
  - Entrypoint: `cat settings.gradle`
  - Observable: `customer-service-api-messaging` is no longer in `settings.gradle`; its classes are in `customer-service-messaging`
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [x] Copy classes from `customer-service-api-messaging/src/main/java` into `customer-service-messaging/src/main/java`
    - [x] Update `customer-service-messaging/build.gradle` to include any dependencies from `customer-service-api-messaging/build.gradle`
    - [x] Replace `project(":customer-service-api-messaging")` dependency with inlined dependencies
    - [x] Remove `customer-service-api-messaging` from `settings.gradle`
    - [x] Delete the `customer-service-api-messaging/` directory

- [x] **Task 2.3: Reorganize into nested customer-management structure**
  - TaskType: OUTCOME
  - Entrypoint: `cat settings.gradle`
  - Observable: Subprojects are `customer-management:customer-management-domain`, `customer-management:customer-management-web-api`, `customer-management:customer-management-command-api`, `customer-management:customer-management-persistence`; `customer-service-main` remains top-level
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [x] Create `customer-management/` directory
    - [x] Move `customer-service-domain/` to `customer-management/customer-management-domain/`
    - [x] Move `customer-service-web/` to `customer-management/customer-management-web-api/`
    - [x] Move `customer-service-messaging/` to `customer-management/customer-management-command-api/`
    - [x] Move `customer-service-persistence/` to `customer-management/customer-management-persistence/`
    - [x] Update `settings.gradle` with new nested include paths
    - [x] Update all `project(":customer-service-*")` references in build.gradle files to new paths
    - [x] Update all `project(":customer-service-*")` references in `customer-service-main/build.gradle`

---

## Steel Thread 3: Domain Event Publishing

Add domain event publishing to the customer-management domain following the `DomainEventPublisherForAggregate` pattern.

- [x] **Task 3.1: Add domain event classes and publisher interface**
  - TaskType: OUTCOME
  - Entrypoint: `find customer-management/customer-management-domain/src -name '*Event*'`
  - Observable: `CustomerEvent` interface, `CustomerCreditReservedEvent` record, and `CustomerEventPublisher` interface exist in domain
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [x] Add `api 'io.eventuate.tram.core:eventuate-tram-spring-events'` to `customer-management-domain/build.gradle`
    - [x] Create `CustomerEvent` interface extending `DomainEvent` in domain package
    - [x] Create `CustomerCreditReservedEvent` record implementing `CustomerEvent`
    - [x] Create `CustomerEventPublisher` interface extending `DomainEventPublisherForAggregate<Customer, Long, CustomerEvent>`

- [x] **Task 3.2: Create event-publishing adapter module**
  - TaskType: OUTCOME
  - Entrypoint: `cat settings.gradle`
  - Observable: `customer-management:customer-management-event-publishing` module exists with `CustomerEventPublisherImpl` and configuration
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [x] Create `customer-management/customer-management-event-publishing/` directory structure
    - [x] Add `customer-management:customer-management-event-publishing` to `settings.gradle`
    - [x] Create `build.gradle` with dependencies on domain, `eventuate-tram-spring-events-publisher-starter`, `eventuate-tram-spring-flyway`
    - [x] Create `CustomerEventPublisherImpl` extending `AbstractDomainEventPublisherForAggregateImpl`
    - [x] Create `CustomerEventPublishingConfiguration` with `@Bean` for the publisher

- [x] **Task 3.3: Inject publisher into CustomerService**
  - TaskType: OUTCOME
  - Entrypoint: `cat customer-management/customer-management-domain/src/main/java/**/CustomerService.java`
  - Observable: `CustomerService.reserveCredit()` publishes `CustomerCreditReservedEvent` after reserving credit
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [x] Add `CustomerEventPublisher` as constructor parameter in `CustomerService`
    - [x] Update `CustomerDomainConfiguration` to pass publisher to `CustomerService`
    - [x] Add `customerEventPublisher.publish(customer, new CustomerCreditReservedEvent(orderId))` after `customer.reserveCredit()`
    - [x] Update existing tests that construct `CustomerService` to pass a mock publisher

---

## Steel Thread 4: Domain Event Subscription

Add a domain event consumer for `CustomerCreditReservedEvent` following the `@EventuateDomainEventHandler` pattern.

- [ ] **Task 4.1: Create event-subscribers adapter module**
  - TaskType: OUTCOME
  - Entrypoint: `cat settings.gradle`
  - Observable: `customer-management:customer-management-event-subscribers` module exists with consumer and configuration
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [ ] Create `customer-management/customer-management-event-subscribers/` directory structure
    - [ ] Add `customer-management:customer-management-event-subscribers` to `settings.gradle`
    - [ ] Create `build.gradle` with dependencies on domain, `eventuate-tram-spring-events-subscriber-starter`
    - [ ] Create `CustomerCreditReservedEventConsumer` with `@EventuateDomainEventHandler` method
    - [ ] Create `CustomerEventSubscribersConfiguration` with `@Bean` for the consumer

- [ ] **Task 4.2: Add unit test for event consumer**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew :customer-management:customer-management-event-subscribers:test`
  - Observable: Unit test passes — publishes event via in-memory Tram and verifies handler is invoked
  - Evidence: `./gradlew :customer-management:customer-management-event-subscribers:test`
  - Steps:
    - [ ] Add unit test dependencies: `eventuate-tram-spring-in-memory`, `eventuate-tram-spring-events-publisher-starter`, `awaitility`, `h2`
    - [ ] Create unit test using `TramInMemoryConfiguration` + `@MockitoBean` + Awaitility

- [ ] **Task 4.3: Add integration test for event consumer**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew :customer-management:customer-management-event-subscribers:integrationTest`
  - Observable: Integration test passes with real Kafka and PostgreSQL via TestContainers
  - Evidence: `./gradlew :customer-management:customer-management-event-subscribers:integrationTest`
  - Steps:
    - [ ] Apply `io.eventuate.plugins.gradle.testing.integration-tests` plugin
    - [ ] Add integration test dependencies: `eventuate-common-testcontainers`, `eventuate-messaging-kafka-testcontainers`, `awaitility`, TestContainers PostgreSQL
    - [ ] Create integration test using TestContainers

---

## Steel Thread 5: Service Main — API Docs and Component Tests

Add Springwolf async API documentation and API docs generation test to `customer-service-main`.

- [ ] **Task 5.1: Add Springwolf and springdoc-openapi dependencies**
  - TaskType: OUTCOME
  - Entrypoint: `cat customer-service-main/build.gradle`
  - Observable: Build.gradle includes `springdoc-openapi-starter-webmvc-ui`, `eventuate-tram-springwolf-support-starter`, and test dependencies for in-memory API docs test
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [ ] Add `springdoc-openapi-starter-webmvc-ui` implementation dependency
    - [ ] Add `eventuate-tram-springwolf-support-starter` implementation dependency
    - [ ] Add `eventuate-tram-springwolf-support-testing` and `eventuate-tram-spring-in-memory` test dependencies
    - [ ] Add `h2` testRuntimeOnly dependency
    - [ ] Add event-publishing and event-subscribers module dependencies

- [ ] **Task 5.2: Add API docs generation test**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew :customer-service-main:test`
  - Observable: `GenerateApiDocsIntegrationTest` passes, writing `build/api-docs/openapi.json` and `build/api-docs/asyncapi.json`
  - Evidence: `./gradlew :customer-service-main:test`
  - Steps:
    - [ ] Create `GenerateApiDocsIntegrationTest` in `customer-service-main/src/test/java`
    - [ ] Configure with `TramInMemoryConfiguration`, exclude `FlywayAutoConfiguration`
    - [ ] Add test for OpenAPI docs (`/v3/api-docs`) asserting domain-specific endpoints
    - [ ] Add test for AsyncAPI docs (`/springwolf/docs`) asserting domain-specific channels

- [ ] **Task 5.3: Add component test outbox verification dependencies**
  - TaskType: OUTCOME
  - Entrypoint: `cat customer-service-main/build.gradle`
  - Observable: Component test dependencies include `eventuate-tram-spring-testing-support-outbox` and `eventuate-tram-spring-testing-support-producer-kafka`
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [ ] Add `eventuate-tram-spring-testing-support-outbox` to componentTestImplementation
    - [ ] Add `eventuate-tram-spring-testing-support-producer-kafka` to componentTestImplementation

- [ ] **Task 5.4: Update TestContainers to use JUnit Jupiter lifecycle**
  - TaskType: OUTCOME
  - Entrypoint: `grep -r junit-jupiter . --include='*.gradle' || echo not found`
  - Observable: Persistence integration tests use `org.testcontainers:junit-jupiter`
  - Evidence: `./gradlew compileAll`
  - Steps:
    - [ ] Add `org.testcontainers:junit-jupiter` to persistence integration test dependencies
    - [ ] Update persistence integration tests to use `@Testcontainers` and `@Container` annotations

---

## Change History
### 2026-04-24 08:37 - mark-task-complete
Updated eventuatePlatformVersion to 2026.1.BUILD-SNAPSHOT, verified with ./gradlew compileAll - BUILD SUCCESSFUL

### 2026-04-24 21:36 - mark-step-complete
Created customer-management/ directory

### 2026-04-24 21:36 - mark-step-complete
Moved customer-service-domain/ to customer-management/customer-management-domain/

### 2026-04-24 21:36 - mark-step-complete
Moved customer-service-web/ to customer-management/customer-management-web-api/

### 2026-04-24 21:36 - mark-step-complete
Moved customer-service-messaging/ to customer-management/customer-management-command-api/

### 2026-04-24 21:36 - mark-step-complete
Moved customer-service-persistence/ to customer-management/customer-management-persistence/

### 2026-04-24 21:36 - mark-step-complete
Updated settings.gradle with nested include paths

### 2026-04-24 21:36 - mark-step-complete
Updated all project references in submodule build.gradle files

### 2026-04-24 21:36 - mark-step-complete
Updated all project references in customer-service-main/build.gradle

### 2026-04-24 21:36 - mark-task-complete
Reorganized into nested customer-management structure. compileAll passes.

### 2026-04-24 21:55 - mark-task-complete
Added CustomerEvent, CustomerCreditReservedEvent, CustomerEventPublisher, and eventuate-tram-spring-events dependency

### 2026-04-25 08:38 - mark-task-complete
Created event-publishing adapter module with CustomerEventPublisherImpl and CustomerEventPublishingConfiguration, compileAll passes
