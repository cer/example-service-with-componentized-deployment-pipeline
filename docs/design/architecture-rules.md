# Architecture rules

This document defines the dependency rules for a service built from this template. It is the specification that any enforcement mechanism (Gradle dependency checks, ArchUnit tests, or similar) implements. Enforcement is a separate, later step.

Each rule has an identifier so that a check can name the rule it verifies.

The subdomain-to-subdomain rules follow the [customers-and-orders modular monolith example](https://github.com/cer/modular-monolith-examples-customers-and-orders), which this template should be able to express: subdomains collaborate by invoking each other's API interfaces and by the Observer pattern, and a subdomain's clients depend only on its API projects.

## Vocabulary

### Service

The deployable unit built from this repository. It consists of one or more subdomains, zero or more shared libraries, and one main project. This repository has a single subdomain, `customer-management`, which is the pattern to replicate for each real subdomain.

### Root package

`<tld>.<company>.<service>`, here `io.eventuate.customerservice`. Written `<root>` below.

### Subdomain

A slice of business functionality. A subdomain is a directory containing one Gradle subproject per element of its hexagonal architecture. Every leaf project name is prefixed with the subdomain name, for example `customer-management-domain`.

### Shared library

A project outside any subdomain that holds value types used by several subdomains, such as a `money` project. It contains no business logic, no Spring configuration and no persistence. This template takes `Money` from an external library and has no shared library project.

### Domain

The projects that are not adapters:

| Project | Package | Contents |
|---|---|---|
| `<subdomain>-api` | `<root>.<subdomain>.api` | Interfaces and value types that other subdomains call: the subdomain's shared-code API |
| `<subdomain>-api-<facet>` | `<root>.<subdomain>.api.<facet>` | A narrower API for one group of clients, for example `-api-credit-management`, or `-api-observer` for the Observer pattern |
| `<subdomain>-domain` | `<root>.<subdomain>.domain` | Entities, value types, domain services (implementing the API interfaces), published event classes, outbound port interfaces |
| `<subdomain>-sagas` | `<root>.<subdomain>.sagas` | Saga orchestrators, saga data, participant proxies, the service that starts sagas |

A subdomain has API projects only when another subdomain calls it in-process. Several API projects per subdomain are allowed and preferred, so that each client depends only on the operations it uses (Interface Segregation Principle).

### Inbound adapter

A project that receives requests from one external technology and calls the domain.

| Project | Package | Contents |
|---|---|---|
| `<subdomain>-rest-api` | `<root>.<subdomain>.restapi` | REST controllers, request/response DTOs, security configuration |
| `<subdomain>-command-api` | `<root>.<subdomain>.commandapi` | Saga participant command handlers, command and reply classes |
| `<subdomain>-event-subscribers` | `<root>.<subdomain>.eventsubscribers` | Handlers for other subdomains' and services' published events |

### Outbound adapter

A project that implements a port declared in `<subdomain>-domain` using one external technology.

| Project | Package | Contents |
|---|---|---|
| `<subdomain>-persistence` | `<root>.<subdomain>.persistence` | JPA configuration, Flyway migrations |
| `<subdomain>-event-publishing` | `<root>.<subdomain>.eventpublishing` | Implementation of the domain's event publisher port |
| `<subdomain>-<other>-proxy` | `<root>.<subdomain>.<other>proxy` | Implementation of a port that calls another service |

### Port

An interface declared in `<subdomain>-domain` that the domain calls and an outbound adapter implements. Current ports: `CustomerRepository`, `CustomerEventPublisher`, `OtherService`.

### Main

The `<service>-main` project. It holds the Spring Boot application class and assembles every subdomain's projects into one deployable. Its package is the root package.

### Events

A subdomain may emit two kinds of events, and the rules treat them differently.

**In-memory events** use the Observer pattern. The emitting subdomain B declares, in `<B>-api-observer`, an observer interface with one method per event (`CustomerDomainObserver.noteCustomerCreated(CustomerInfo)`), a registration interface (`CustomerDomainObservers.registerObserver(...)`), and the value types the methods carry. `<B>-domain` implements the registration interface and calls every registered observer synchronously, in the emitter's transaction. A consuming subdomain A implements the observer interface in `<A>-domain` and registers itself at startup. The observer classes are shared code. This template currently has none.

**Published events** are Eventuate Tram domain events written to the outbox and delivered through the message broker (`CustomerCreatedEvent`, `CustomerCreditReservedEvent`). The event class is never shared: each consumer declares its own copy (see SUB-4).

### Messages

Published events, saga commands, and saga replies. A message's identity on the wire is its fully qualified class name, and its contract is its JSON schema. A message is consumed by copying its class, never by a Gradle dependency on the producer.

### Subdomain API

The part of a subdomain that other subdomains may use. It has two parts.

The **shared-code API** is the contents of the subdomain's API projects, consumed by Gradle dependency and import:

- **Operations.** Interfaces whose methods take and return value types, implemented by a domain service in `<subdomain>-domain`.
- **In-memory events.** The observer and registration interfaces and their value types, in `<subdomain>-api-observer`.

The **message API** is consumed by copying message classes and is verified by contract tests:

- **Published events.** The event classes in `<subdomain>-domain`.
- **Commands and replies.** The command and reply classes in `<subdomain>-command-api`.

Everything else in a subdomain is **internal**: entities and their behaviour, domain service implementations, repositories and other port interfaces, saga classes, every adapter class, and every Spring configuration class. Entities are never part of an API; an operation exposed to another subdomain returns a value type.

The message API is defined this way because the orchestrator and the participant, or the publisher and the subscriber, would normally be in different services. Keeping the consumer's copy separate inside one service preserves that independence and means a subdomain can be moved to its own service without changing its code.

## Hexagonal architecture rules

These rules apply within a single subdomain.

### HEX-1. The domain does not depend on adapters

No domain project declares a Gradle dependency on an adapter project, and no class in a domain package imports a class from an adapter package.

### HEX-2. Adapters depend only on the domain

An adapter project declares Gradle dependencies on domain projects of its own subdomain, on shared libraries, and on third-party libraries. It does not depend on another adapter project of the same subdomain, and no class in an adapter package imports a class from another adapter package.

### HEX-3. Outbound dependencies go through ports

The domain reaches persistence, messaging and other services only through port interfaces declared in `<subdomain>-domain`. An outbound adapter implements a port; the domain never names the implementing class.

### HEX-4. Domain projects stay free of transport and infrastructure technology

Domain projects do not depend on web frameworks (Spring MVC, servlet API), HTTP clients, message broker clients, JDBC, Flyway, or the Eventuate Tram producer, consumer, or command libraries. The permitted framework dependencies in the domain are:

- Spring context annotations (`@Configuration`, `@Bean`, `@Transactional`) for wiring, in `<subdomain>-domain` and `<subdomain>-sagas`.
- JPA annotations on entities and Spring Data repository interfaces, because the template keeps the entity and its mapping in one place.
- The Eventuate Tram events API (`eventuate-tram-spring-events`) for the `DomainEvent` interface.
- The Eventuate Tram Sagas orchestration DSL, in `<subdomain>-sagas` only.
- The Eventuate Tram Flyway migration support (`eventuate-tram-spring-flyway`) and the saga Flyway migrations (`eventuate-tram-sagas-spring-flyway`), in `<subdomain>-sagas` only, because the orchestrator's tables are created by migrations the sagas project enables with `@EnableEventuateTramFlywayMigration`.

Any new dependency added to a domain project must fall into one of these categories or be added to this list.

### HEX-5. One project, one package

Each project declares its classes in its own package, as listed in the tables above, or in a sub-package of it, and in no other package. The single exception is a consumer-side copy of message classes (SUB-4), which is declared in the producer's package because the class name is the message type.

### HEX-6. Each project wires itself

Each project has its own Spring `@Configuration` class that creates its beans. A project does not create beans whose class belongs to another project. API projects have no configuration class.

### HEX-7. Main contains no business logic

Main declares dependencies on every project of every subdomain, either directly or through a per-subdomain aggregate project. Its dependencies on adapter projects are `runtimeOnly` unless main's own code, or a test in main, references an adapter class. Main's own source contains only the application class, cross-cutting configuration, and tests.

### HEX-8. API projects contain only interfaces and value types

An API project contains interfaces, records and enums. It contains no classes with behaviour beyond those, no entities, no Spring configuration, and no annotations from Spring, JPA or Eventuate. An API project depends only on other API projects of the same subdomain and on shared libraries. `<subdomain>-domain` depends on the subdomain's API projects and implements their interfaces.

### HEX-9. The message broker is declared in main only

The message broker artifacts, meaning `eventuate-tram-spring-jdbc-kafka`, `eventuate-tram-spring-consumer-kafka` and their equivalents for ActiveMQ, RabbitMQ or Redis, are declared in main, as `implementation`, and in no other project's main source set in any configuration. The JDBC-side artifacts, such as `eventuate-tram-spring-consumer-jdbc` for idempotency and `eventuate-tram-spring-producer-jdbc` for the outbox, are not broker artifacts and are outside this rule. A subdomain project whose integration or component tests need a running broker declares the broker artifact in that test configuration, `integrationTestImplementation` or `componentTestImplementation`. Main's own tests get it from main's classpath. The choice of broker is a deployment concern of the service; adapters code against the Eventuate Tram producer, consumer and command APIs, not against the broker.

## Subdomain interaction rules

These rules apply between subdomains in the same service, and between a subdomain and another service.

### SUB-1. Subdomains interact only through APIs

A class in subdomain A imports a class from subdomain B only if that class is in one of B's API projects. A never imports B's entities, domain services, repositories, ports, adapter classes, configuration classes, or message classes.

### SUB-2. Gradle dependencies between subdomains are on API projects only

A project in subdomain A declares a Gradle dependency on a project in subdomain B only if that project is one of B's API projects. A never depends on `<B>-domain`, `<B>-sagas`, or any adapter project of B, including `<B>-command-api`. This holds for test source sets too: A's tests use mocks of B's API interfaces, not B's implementation.

### SUB-3. Four interaction mechanisms

Subdomain A consumes subdomain B in one of four ways, and each uses a specific project of A:

| Mechanism | Where in A | What of B it depends on | How |
|---|---|---|---|
| In-process operation call | `<A>-domain` or `<A>-sagas` | `<B>-api` or `<B>-api-<facet>` | Gradle dependency, import, call the interface |
| In-memory event | `<A>-domain` | `<B>-api-observer` | Gradle dependency, implement the observer interface, register at startup |
| Published event subscription | `<A>-event-subscribers` | Nothing | Copy of the event class, contract test |
| Command/reply messaging | `<A>-sagas` sends, `<B>-command-api` receives | Nothing | Copy of the command and reply classes, contract test |

### SUB-4. Messages are copied, not shared

When a subdomain consumes a published event or a command/reply, whether from another subdomain in this service or from another service, the consuming project declares its own copy of the message classes in the producer's package. The copy carries no handler or reply annotations. Its compatibility with the producer is verified by a contract test against the producer's published stubs. This applies even when producer and consumer are in the same subdomain, as with a saga orchestrator and its own subdomain's participant. Current examples:

- `OtherEvent` in `customer-management-event-subscribers`, copied from `other-service`.
- The `ReserveInventoryCommand` family in `customer-management-sagas`, copied from `other-service`.
- The `ReserveCreditCommand` family in `customer-management-sagas`, copied from `customer-management-command-api`.

### SUB-5. Each subdomain owns its data

A subdomain's persistence adapter maps only that subdomain's entities, and its Flyway migrations create only that subdomain's tables. No subdomain queries another subdomain's tables, by JPA or by SQL.

### SUB-6. No cycles between subdomains via shared code

The graph whose nodes are subdomains and whose edges are Gradle dependencies on API projects is acyclic. Published events and command/reply messaging may form cycles, because they are asynchronous and share no code.

### SUB-7. Main is the only composition root

Only main depends on the adapter projects of more than one subdomain. No subdomain project depends on main.

### SUB-8. Shared libraries hold only value types

Any project may depend on a shared library. A shared library depends on no subdomain project and no other shared library, and contains only value types.

## Current state against the rules

Checked on 2026-10-22 against every project in this repository, `main` source sets and build files.

### Satisfied

- **HEX-1.** `customer-management-domain` and `customer-management-sagas` depend on no adapter project and import no adapter package.
- **HEX-2.** Every adapter depends on `customer-management-domain` only, plus `customer-management-rest-api` on `customer-management-sagas`, which is a domain project. No adapter imports another adapter's package.
- **HEX-3.** The domain reaches persistence, events and `other-service` through `CustomerRepository`, `CustomerEventPublisher` and `OtherService`. It imports no implementing class.
- **HEX-4** for `customer-management-domain`. Its third-party imports are Money, the Eventuate events API (`DomainEvent`, `DomainEventPublisherForAggregate`), JPA annotations, Spring Data `CrudRepository`, `@Transactional`, Spring `@Configuration`/`@Bean` and SLF4J, all permitted.
- **HEX-5.** Each project's classes are in its own package or a sub-package (`sagas.proxies`). The classes outside that are the SUB-4 copies: `othersubdomain.domain.OtherEvent` in `customer-management-event-subscribers`, and the `customermanagement.commandapi` and `othersubdomain.commandapi` copies in `customer-management-sagas`.
- **HEX-6.** Each project has one `@Configuration` class, and every `@Bean` it declares is of a class in that project or a third-party class.
- **SUB-4.** The three copies listed under the rule carry no handler or reply annotations and each has a contract test.
- **HEX-4** for `customer-management-sagas`. Its build file declares the sagas DSL and the Eventuate Flyway migration support, both permitted in sagas.
- **HEX-9.** Only `customer-service-main` declares a message broker artifact in a main source set. `customer-management-sagas` and `customer-management-command-api` declare the JDBC/Kafka starter `integrationTestImplementation` only.
- **HEX-7.** `customer-service-main` declares no `implementation` project dependency. Its dependencies on every `customer-management` project are `runtimeOnly`. `CustomerServiceComponentTest` gets `customer-management-domain`, `customer-management-command-api`, `customer-management-sagas` and `customer-management-event-subscribers` through `componentTestImplementation`.
- **SUB-5, SUB-7.** Satisfied with one subdomain.

### Violated

None.

### Not demonstrated

- **HEX-8, SUB-1, SUB-2, SUB-3, SUB-6.** There is no API project and no second subdomain, so nothing calls `customer-management` in-process. `CustomerManagementService` is a concrete class, so a second subdomain could not depend on it under SUB-2 without first extracting an API project. See open question 1.
- **SUB-8.** No shared library exists.

### Outside the rules

- `other-service` is a stand-in for a separate service, delivered by composite build, and the rules do not apply to it. For the record, its `other-subdomain-event-publishing` project declares `OtherEvent` in the `othersubdomain.domain` package, which HEX-5 would forbid in this repository.
- `CustomerServiceComponentTest` in main imports `CustomerServiceProxy` from the `sagas.proxies` package, an internal class of the sagas project. No rule covers tests in main.
- `customer-management-event-subscribers` pins the `other-service` stubs to a literal `0.2.0-SNAPSHOT` while `customer-management-sagas` uses the `otherServiceStubsVersion` property. Not an architecture rule, noted in passing.

## Open questions

1. **Should the template include an API project and an observer now?** The rules describe API projects and the Observer pattern, but the template has neither, so a user copying the template has no example to follow and no enforcement check exercises HEX-8 or SUB-1 to SUB-3. The alternatives are: add a `customer-management-api` project with a `CustomerManagementService` interface and a `customer-management-api-observer` project now, with a second subdomain that uses them; or leave them until a real second subdomain needs them.

## Decisions recorded

- Published events, commands and replies are copied by the consumer, never shared, because producer and consumer would normally be in different services (SUB-4).
- Entities are internal. Operations in an API return value types.
- The shared-code API is a set of dedicated API projects, not a package inside the domain project, following the customers-and-orders example.
- Observers run synchronously in the emitter's transaction.
- Test source sets follow SUB-2: a subdomain's tests depend on other subdomains' API projects only.
- The sagas project keeps the Eventuate Flyway migration support and the saga migrations (HEX-4 option A, 2026-10-22). Moving them to main or to a new adapter project was rejected as either coupling the orchestrator to main or adding a near-empty project.
- The message broker artifacts are declared in main only (HEX-9, 2026-10-22). `spring-boot-starter-flyway` and `flyway-database-postgresql` are never declared explicitly, because `eventuate-tram-spring-flyway` declares both at compile scope.
- `other-service` stands in for a separate service and is outside the rules (2026-10-22). It keeps `OtherEvent` in its `other-subdomain-event-publishing` project, in the `othersubdomain.domain` package.

## TODO
