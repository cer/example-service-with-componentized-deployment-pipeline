# Architecture rules enforcement

## Problem

[docs/design/architecture-rules.md](../../../design/architecture-rules.md) defines the hexagonal rules HEX-1 to HEX-9 and the subdomain rules SUB-1 to SUB-8 that every service built from this template follows. Nothing checks them. The rules document's current-state section records one violation in this repository: HEX-7 (main contains no business logic) in `customer-service-main/build.gradle`. A developer can add a forbidden project dependency, or a `@Configuration` class to an API project, and the build passes.

## Goal

The Gradle build fails on a violation of any rule the design marks as enforced, in this repository and in every service the skeleton generator produces from it. The design is [docs/design/architecture-rules-enforcement.md](../../../design/architecture-rules-enforcement.md): a common plugin applied to every subproject that checks project dependencies, a role plugin per project that declares the project's role, and ArchUnit rules run against each project's compiled classes, all wired into `check`.

## Locations

### Specification and design

- `docs/design/architecture-rules.md` — the rules, the roles vocabulary, the current-state violations and the TODO section that must be resolved first
- `docs/design/architecture-rules-enforcement.md` — the design: approach, roles table, rules implementation, initial implementation, decisions

### Definition

- `buildSrc/build.gradle` — gains `java-gradle-plugin` and the role plugin ids
- `buildSrc/src/main/groovy/` — the common plugin and the role plugins, next to `ServicePlugin.groovy`
- `architecture-rules/` — new subproject holding the ArchUnit rules

### Wiring

- `settings.gradle:15-24` — includes the new subproject
- `build.gradle:23` — the `subprojects` block applies the common plugin and exempts `architecture-rules`

### Build files converted to role plugins

- `customer-management/customer-management-domain/build.gradle`
- `customer-management/customer-management-sagas/build.gradle`
- `customer-management/customer-management-rest-api/build.gradle`
- `customer-management/customer-management-command-api/build.gradle`
- `customer-management/customer-management-event-subscribers/build.gradle`
- `customer-management/customer-management-persistence/build.gradle`
- `customer-management/customer-management-event-publishing/build.gradle`
- `customer-management/customer-management-other-service-proxy/build.gradle`
- `customer-service-main/build.gradle`

### Unchanged

- `other-service/` — a separate build delivered by `includeBuild`, outside the rules
