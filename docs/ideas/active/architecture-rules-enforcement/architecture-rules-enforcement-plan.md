# Implementation Plan: Architecture rules enforcement

## Idea Type

**C. Platform/infrastructure capability** — The capability is consumed by the service's Gradle build files, by developers adding projects, and by the skeleton generator that copies the template. End users of the service see nothing.

## Instructions for Coding Agent

- IMPORTANT: Use simple commands that you have permission to execute. Avoid complex commands that may fail due to permission issues.

### Required Skills

Use these skills by invoking them before the relevant action:

| Skill | When to Use |
|-------|-------------|
| `idea-to-code:plan-tracking` | ALWAYS - track task completion in the plan file |
| `idea-to-code:tdd` | When implementing code - write failing tests first |
| `idea-to-code:commit-guidelines` | Before creating any git commit |
| `idea-to-code:incremental-development` | When writing multiple similar files (tests, classes, configs) |
| `idea-to-code:testing-scripts-and-infrastructure` | When building shell scripts or test infrastructure |
| `idea-to-code:dockerfile-guidelines` | When creating or modifying Dockerfiles |
| `idea-to-code:file-organization` | When moving, renaming, or reorganizing files |
| `idea-to-code:debugging-ci-failures` | When investigating CI build failures |
| `idea-to-code:test-runner-java-gradle` | When running tests in Java/Gradle projects |

### TDD Requirements

- NEVER write production code (`src/main/java/**/*.java`) without first writing a failing test
- Before using Write on any `.java` file in `src/main/`, ask: "Do I have a failing test?" If not, write the test first
- When task direction changes mid-implementation, return to TDD PLANNING state and write a test first

### Verification Requirements

- Hard rule: NEVER git commit, git push, or open a PR unless you have successfully run the project's test command and it exits 0
- Hard rule: If running tests is blocked for any reason (including permissions), ALWAYS STOP immediately. Print the failing command, the exact error output, and the permission/path required
- Before committing, ALWAYS print a Verification section containing the exact test command (NOT an ad-hoc command - it must be a proper test command such as `./test-scripts/*.sh`, `./gradlew build`/`./gradlew check`), its exit code, and the last 20 lines of output

## Overview

`docs/design/architecture-rules.md` defines the hexagonal rules HEX-1 to HEX-9 and the subdomain rules SUB-1 to SUB-8. Nothing checks them today. This plan makes `./gradlew build` fail on a violation of every rule that `docs/design/architecture-rules-enforcement.md` marks as enforced.

The implementation has three parts:

- **Plugins in `buildSrc`.** The common plugin `ArchitectureRulesPlugin` (id `io.eventuate.architecture.rules`) is applied to every subproject by the root `subprojects` block, and to the root project. There is one role plugin per row of the roles table (`io.eventuate.architecture.roles.<role>`). The project dependency rules run in `checkProjectDependencies` in each project and in `checkSubdomainGraph` at the root. Neither task compiles anything.
- **ArchUnit rules in a new `architecture-rules` subproject.** They are run per project by the `checkArchitectureRules` `Test` task against the project's `build/classes/java/main`.
- **Build files converted to role plugins.** The HEX-7 violation in `customer-service-main/build.gradle` is fixed first, so the checks pass on their first run.

What already exists and is reused:

- `.github/workflows/ci.yaml` runs the build through `cer/github-build-and-test-action/build-and-test`.
- `buildSrc/build.gradle` already reads `../gradle.properties`.
- `ServicePlugin` already applies the Spring Boot and Docker plugins to main.
- `settings.gradle` declares the integration-tests and component-tests plugin versions in `pluginManagement`.

There is no design pattern catalog in this repository.

Code conventions used throughout:

- `buildSrc` code lives in `buildSrc/src/main/groovy/io/eventuate/architecture/rules/gradle/`, with tests in `buildSrc/src/test/groovy/io/eventuate/architecture/rules/gradle/`. Tests are JUnit Jupiter tests that use Gradle's `ProjectBuilder`.
- ArchUnit code lives in `architecture-rules/src/main/java/io/eventuate/architecture/rules/`, with tests and fixture classes in `architecture-rules/src/test/java/io/eventuate/architecture/rules/`.
- Every rule test class is named after its rule identifier.
- Every failure message starts with the rule identifier and its title from the rules document, for example `HEX-1 (the domain does not depend on adapters)`, followed by the offending item.
- No code in `buildSrc/src/main` or `architecture-rules/src/main` names a project of this repository. Every rule is a function of role, group and subdomain. The root package is the project's `group`.

Since Gradle 8, `buildSrc` tests do not run as part of the main build. They run with `./gradlew -p buildSrc check`, which CI runs as its own step from Task 1.2 onwards.

## Steel Thread 1: Every project declares its role and the build checks HEX-1

- [x] **Task 1.1: The service builds with main's dependencies on domain, command-api and sagas at runtimeOnly scope**
  - TaskType: INFRA
  - Entrypoint: `./gradlew build`
  - Observable: `customer-service-main/build.gradle` declares no `implementation` project dependency. `customer-management-domain`, `customer-management-command-api` and `customer-management-sagas` are declared `runtimeOnly` and `componentTestImplementation`. `CustomerServiceComponentTest` and every other test pass. In the rules document, the current-state section records no violation and the TODO section has no entries.
  - Evidence: `./gradlew build` exits 0, including `:customer-service-main:componentTest`. CI runs the build on push.
  - Steps:
    - [x] Run `./gradlew build` to confirm that the existing build and CI baseline passes before any change.
    - [x] In `customer-service-main/build.gradle`, change the three `implementation project(...)` lines for `customer-management-domain`, `customer-management-command-api` and `customer-management-sagas` to `runtimeOnly`. Add `componentTestImplementation` for the same three projects, next to the existing `componentTestImplementation` on `customer-management-event-subscribers`.
    - [x] Run `./gradlew :customer-service-main:componentTest`, then `./gradlew build`.
    - [x] In `docs/design/architecture-rules.md`, move HEX-7 from "Violated" to "Satisfied". Replace the "Violated" list with "None."
    - [x] In `docs/design/architecture-rules.md`, delete the "Resolve the HEX-7 violation" TODO entry. Delete the "Decide whether `other-service` follows HEX-5" TODO entry, and add a "Decisions recorded" bullet saying that `other-service` stands in for a separate service, is outside the rules, and keeps `OtherEvent` in its event-publishing project. Leave the `## TODO` heading with no entries.

- [x] **Task 1.2: Each build file declares its role with one role plugin line, and the role plugin applies the role's build conventions**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew build`
  - Observable: Each of the nine build files applies exactly one `io.eventuate.architecture.roles.<role>` plugin, and each project's `architectureRules.role` extension property holds that role. The adapter roles and `sagas` have an `integrationTest` task from `io.eventuate.plugins.gradle.testing.integration-tests`. `main` has `bootJar` from `ServicePlugin` and `componentTest` from `io.eventuate.plugins.gradle.testing.component-tests`. No build file applies the integration-tests plugin, the component-tests plugin or `ServicePlugin` directly. The build passes.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.RolePluginsTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [x] Create `buildSrc/settings.gradle` containing `rootProject.name = 'buildSrc'`, so that `./gradlew -p buildSrc` runs `buildSrc` as a standalone build.
    - [x] Update `buildSrc/build.gradle`:
      - Apply `groovy` and `java-gradle-plugin`.
      - Add `gradlePluginPortal()` and the repositories in `rootGradleProperties.eventuateMavenRepoUrl`.
      - Add `implementation` dependencies on the plugin marker artifacts `io.eventuate.plugins.gradle.testing.integration-tests:io.eventuate.plugins.gradle.testing.integration-tests.gradle.plugin` and `io.eventuate.plugins.gradle.testing.component-tests:io.eventuate.plugins.gradle.testing.component-tests.gradle.plugin`, at `rootGradleProperties.eventuateTestPluginsVersion`.
      - Add JUnit Jupiter `testImplementation` and `junit-platform-launcher` `testRuntimeOnly` dependencies, and `test { useJUnitPlatform() }`.
    - [x] Write `RolePluginsTest`. For each role id it applies the plugin to a `ProjectBuilder` project and asserts the role on the extension. It asserts that `integrationTest` exists for the six adapter roles and `sagas`, and that `bootJar` and `componentTest` exist for `main`.
    - [x] Create these classes in `buildSrc/src/main/groovy/io/eventuate/architecture/rules/gradle/`:
      - `RoleGroup`: an enum with `DOMAIN`, `ADAPTER`, `MAIN` and `SHARED_LIBRARY`.
      - `Role`: an enum holding the roles table from `docs/design/architecture-rules-enforcement.md`, with the role name, group, project name pattern and package pattern.
      - `ArchitectureRulesExtension`: holds a `role` property.
      - `ArchitectureRulesPlugin`: creates the `architectureRules` extension.
    - [x] Create the abstract `RolePlugin`. It applies `ArchitectureRulesPlugin` (applying it twice has no effect), sets the role, and calls an `applyConventions(Project)` hook. Create the twelve role plugin classes, one per role: `ApiRolePlugin`, `ApiFacetRolePlugin`, `DomainRolePlugin`, `SagasRolePlugin`, `RestApiRolePlugin`, `CommandApiRolePlugin`, `EventSubscribersRolePlugin`, `PersistenceRolePlugin`, `EventPublishingRolePlugin`, `ProxyRolePlugin`, `MainRolePlugin` and `SharedLibraryRolePlugin`. The adapter roles and `sagas` apply `io.eventuate.plugins.gradle.testing.integration-tests`. `main` applies `ServicePlugin` and `io.eventuate.plugins.gradle.testing.component-tests`.
    - [x] Declare the twelve role plugin ids and the common plugin id `io.eventuate.architecture.rules` in the `gradlePlugin` block of `buildSrc/build.gradle`.
    - [x] Add `apply plugin: 'io.eventuate.architecture.rules'` to the `subprojects` block in `build.gradle`.
    - [x] Convert the build files:
      - `customer-management/customer-management-domain/build.gradle`: add `plugins { id 'io.eventuate.architecture.roles.domain' }`.
      - `customer-management/customer-management-sagas/build.gradle`: apply `io.eventuate.architecture.roles.sagas` in place of the integration-tests plugin. Keep the contract plugin lines.
      - `customer-management/customer-management-rest-api/build.gradle`: add `io.eventuate.architecture.roles.rest-api`. Keep the contract and `PublishStubsPlugin` lines.
      - `customer-management/customer-management-command-api/build.gradle`: apply `io.eventuate.architecture.roles.command-api` in place of the integration-tests plugin.
      - `customer-management/customer-management-event-subscribers/build.gradle`: apply `io.eventuate.architecture.roles.event-subscribers` in place of the integration-tests plugin.
      - `customer-management/customer-management-persistence/build.gradle`: apply `io.eventuate.architecture.roles.persistence` in place of the integration-tests plugin.
      - `customer-management/customer-management-event-publishing/build.gradle`: apply `io.eventuate.architecture.roles.event-publishing` in place of the integration-tests plugin. Keep the contract and `PublishStubsPlugin` lines.
      - `customer-management/customer-management-other-service-proxy/build.gradle`: add `io.eventuate.architecture.roles.proxy`.
      - `customer-service-main/build.gradle`: apply `io.eventuate.architecture.roles.main` in place of the component-tests plugin and `apply plugin: ServicePlugin`.
    - [x] Remove the `plugins { }` block that declares the two test plugin versions from `pluginManagement` in `settings.gradle`. The test plugins now come from the `buildSrc` classpath, and a version request for a plugin already on the classpath fails.
    - [x] Add a step `./gradlew -p buildSrc check` to `.github/workflows/ci.yaml`, before "Build and test".
    - [x] Add an "Architecture rules" section to `README.md`. It says that each project applies one role plugin, lists the role plugin ids, and links `docs/design/architecture-rules.md`.
    - [x] Run `./gradlew -p buildSrc check`, then `./gradlew build`.

- [x] **Task 1.3: `checkProjectDependencies` fails a domain project that depends on an adapter, naming HEX-1, before compilation**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkProjectDependencies`
  - Observable: In a build where `customer-management-domain` declares `implementation project(':customer-management:customer-management-persistence')`, `checkProjectDependencies` fails with a message that starts `HEX-1 (the domain does not depend on adapters)` and names the dependency path and the configuration `implementation`. `check` in every project with a role depends on `checkProjectDependencies`. Running `checkProjectDependencies` executes no `compileJava` task.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.Hex1DomainDoesNotDependOnAdaptersTest` exits 0, `./test-scripts/test-end-to-end.sh` exits 0, and `./gradlew build` exits 0.
  - Steps:
    - [x] Create the test helper `RuleFixtureBuild` in `buildSrc/src/test/groovy/io/eventuate/architecture/rules/gradle/`. It builds a root project and subdomain directories with `ProjectBuilder`, applies role plugins, adds project and external dependencies to named configurations, evaluates the projects, and runs a project's `checkProjectDependencies` action, returning the failure message.
    - [x] Write `Hex1DomainDoesNotDependOnAdaptersTest`. A domain project with `implementation` on a persistence project fails with a message naming HEX-1, the target path and `implementation`. The same holds for `testImplementation`. A domain project with no adapter dependency passes.
    - [x] Create these classes:
      - `Rule`: an enum of rule identifiers with their titles from `docs/design/architecture-rules.md`.
      - `RuleViolation`: holds a rule and a message.
      - `DeclaredDependencies`: a model of a project's declared project dependencies and external dependencies per configuration. It reads `configuration.dependencies` without resolving them, and takes project paths from `ProjectDependency.getPath()`.
      - `ProjectDependencyRule`: an interface returning a list of violations.
      - `Hex1DomainDoesNotDependOnAdapters`.
      - `CheckProjectDependenciesTask`: runs every `ProjectDependencyRule` and fails with all violations, one per line.
    - [x] In `ArchitectureRulesPlugin`, register `checkProjectDependencies` in `afterEvaluate` for every project with a role, and make `check` depend on it.
    - [x] Create `test-scripts/test-project-dependency-checks-do-not-compile.sh`. It runs `./gradlew checkProjectDependencies --dry-run`, asserts that the output lists `:customer-management:customer-management-domain:checkProjectDependencies`, and asserts that it lists no `compileJava` task.
    - [x] Create `test-scripts/test-end-to-end.sh`, which runs each `test-scripts/test-*.sh` behaviour script in sequence. Add a CI step to `.github/workflows/ci.yaml` that runs `./test-scripts/test-end-to-end.sh` after "Build and test".
    - [x] Run `./gradlew -p buildSrc check`, `./test-scripts/test-end-to-end.sh` and `./gradlew build`.

---

## Steel Thread 2: A project added without a role fails the build

- [x] **Task 2.1: A project whose build file applies no role plugin fails the build after evaluation, listing the role plugin ids**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew build`
  - Observable: A project such as `customer-management/customer-management-payment-proxy` whose build file applies no role plugin fails configuration. The message states that the project has no architecture role and lists all twelve `io.eventuate.architecture.roles.*` ids. Removing the role plugin line from any build file fails the build the same way.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.ProjectWithoutRoleTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [x] Write `ProjectWithoutRoleTest`. A subproject with the common plugin and no role plugin fails evaluation with the message naming the project path and the twelve ids. A subproject with a role plugin evaluates.
    - [x] In `ArchitectureRulesPlugin.afterEvaluate`, throw a `GradleException` when no role is set. The message lists the ids from `Role`.
    - [x] Run `./gradlew -p buildSrc check` and `./gradlew build`.

---

## Steel Thread 3: A misnamed project fails the build

- [x] **Task 3.1: A project whose name does not match its role's pattern, or a domain or adapter project outside a subdomain directory, fails the build**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew build`
  - Observable: `customer-management/customer-proxy` applying `io.eventuate.architecture.roles.proxy` fails configuration with a message stating that the name does not match the pattern `<subdomain>-<other>-proxy`. A project with a domain or adapter role whose parent directory is the root directory fails, stating that it is not in a subdomain directory. Every project in this repository passes.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.ProjectNamingTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [x] Write `ProjectNamingTest`, covering: `customer-proxy` with the proxy role fails; `customer-management-other-service-proxy` passes; a domain-role project at the root fails; `x-main` at the root with the main role passes; a shared library at the root passes.
    - [x] Add derivation of the subdomain to the extension. The subdomain is the name of the parent directory. Add derivation of the project package: the root package is the project `group`, and the subdomain and the role suffixes have their hyphens removed, for example `customer-management-other-service-proxy` gives `<root>.customermanagement.otherserviceproxy`.
    - [x] Add the name and directory check to `ArchitectureRulesPlugin.afterEvaluate`, driven by `Role`'s name pattern and group.
    - [x] Run `./gradlew -p buildSrc check` and `./gradlew build`.

---

## Steel Thread 4: A test source set depending on another subdomain's domain fails SUB-2

- [x] **Task 4.1: `checkProjectDependencies` fails a dependency, in any configuration, on a non-API project of another subdomain, naming SUB-2**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkProjectDependencies`
  - Observable: In a build with subdomains `a` and `b`, `testImplementation project(':b:b-domain')` in an `a` project fails with a message starting `SUB-2 (Gradle dependencies between subdomains are on API projects only)` that names `:b:b-domain` and `testImplementation`. A dependency on `:b:b-api` passes this rule.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.Sub2DependenciesBetweenSubdomainsAreOnApiProjectsTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [x] Write `Sub2DependenciesBetweenSubdomainsAreOnApiProjectsTest`, covering: `testImplementation` on `b-domain` fails; `implementation` on `b-command-api` fails; `implementation` on `b-api` and on `b-api-observer` passes.
    - [x] Create `Sub2DependenciesBetweenSubdomainsAreOnApiProjects` and add it to the rules run by `CheckProjectDependenciesTask`.
    - [x] Add a comment in `Sub2DependenciesBetweenSubdomainsAreOnApiProjects` stating that SUB-1 follows from this rule, as the enforcement document says.
    - [x] Run `./gradlew -p buildSrc check` and `./gradlew build`.

---

## Steel Thread 5: A domain project that pulls in infrastructure fails HEX-4

- [ ] **Task 5.1: `checkProjectDependencies` fails an external dependency of a domain-group project outside the permitted categories, naming HEX-4 and the categories**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkProjectDependencies`
  - Observable: `implementation "org.springframework.boot:spring-boot-starter-webmvc"` in a `domain` project fails with a message starting `HEX-4 (domain projects stay free of infrastructure technology)` that names the dependency and lists the permitted categories for the role. `eventuate-tram-spring-flyway` passes in `sagas` and fails in `domain`. The current `customer-management-domain` and `customer-management-sagas` build files pass.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.Hex4DomainDependencyListTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [ ] Write `Hex4DomainDependencyListTest`, covering:
      - `spring-boot-starter-webmvc` in `domain` fails.
      - `runtimeOnly "io.eventuate.tram.sagas:eventuate-tram-sagas-spring-flyway"` passes in `sagas` and fails in `domain`.
      - `spring-boot-starter-data-jpa`, `eventuate-tram-spring-events` and `eventuate-examples-common-money-jakarta9` pass in `domain`.
      - `testImplementation "org.springframework.boot:spring-boot-starter-test"` passes.
    - [ ] Create `PermittedDomainDependencies`, holding the categories as allow-lists of group and artifact:
      - Spring context: `org.springframework:spring-context`, `org.springframework:spring-tx`, `org.springframework.boot:spring-boot-starter`.
      - JPA and Spring Data: `org.springframework.boot:spring-boot-starter-data-jpa`, `org.springframework.data:spring-data-jpa`, `org.springframework.data:spring-data-commons`, `jakarta.persistence:jakarta.persistence-api`.
      - Eventuate events API: `io.eventuate.tram.core:eventuate-tram-spring-events`.
      - Money value type: `io.eventuate.examples.common:eventuate-examples-common-money-jakarta9`.
      - `sagas` only, Eventuate Sagas orchestration DSL: `io.eventuate.tram.sagas:eventuate-tram-sagas-spring-orchestration-simple-dsl-starter`.
      - `sagas` only, Eventuate Flyway migration support: `io.eventuate.tram.core:eventuate-tram-spring-flyway`, `io.eventuate.tram.sagas:eventuate-tram-sagas-spring-flyway`.
    - [ ] Create `Hex4DomainDependencyList`. It checks the main source set configurations `api`, `implementation`, `compileOnly` and `runtimeOnly` of every domain-group project, and adds itself to `CheckProjectDependenciesTask`.
    - [ ] Record in `docs/design/architecture-rules-enforcement.md`, under the HEX-4 dependency list bullet and in "Decisions", that the list applies to the main source set configurations. The reason is that test source sets do not ship and that HEX-9 places broker artifacts in integration test configurations.
    - [ ] Add the Money value type category to the HEX-4 list in `docs/design/architecture-rules.md` and to the HEX-4 dependency list bullet in `docs/design/architecture-rules-enforcement.md`.
    - [ ] Run `./gradlew -p buildSrc check` and `./gradlew build`.

---

## Steel Thread 6: A source file outside the project's package fails HEX-5

- [ ] **Task 6.1: `checkProjectDependencies` fails a source file under `src/main/java` whose path is outside the project's package and its declared message copy packages, naming HEX-5**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkProjectDependencies`
  - Observable: A file `src/main/java/io/eventuate/customerservice/customermanagement/restapi/Foo.java` in the domain project fails with a message starting `HEX-5 (one project, one package)` that names the file. The files in the packages declared by `architectureRules { messageCopyPackages = [...] }` pass. `customer-management-sagas` and `customer-management-event-subscribers` declare their copy packages and pass.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.Hex5OneProjectOnePackageTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [ ] Write `Hex5OneProjectOnePackageTest`. It creates source files under the fixture project's `src/main/java`. A file in the project package and one in a sub-package pass. A file in another package fails. A file in a declared copy package passes.
    - [ ] Add the `messageCopyPackages` list property to `ArchitectureRulesExtension`.
    - [ ] Create `Hex5OneProjectOnePackage`. It walks the `src/main/java` source directories by path, without compiling, and adds itself to `CheckProjectDependenciesTask`. Declare the source tree as task input.
    - [ ] Add `architectureRules { messageCopyPackages = ['io.eventuate.customerservice.customermanagement.commandapi', 'io.eventuate.otherservice.othersubdomain.commandapi'] }` to `customer-management/customer-management-sagas/build.gradle`.
    - [ ] Add `architectureRules { messageCopyPackages = ['io.eventuate.otherservice.othersubdomain.domain'] }` to `customer-management/customer-management-event-subscribers/build.gradle`.
    - [ ] Run `./gradlew -p buildSrc check` and `./gradlew build`.

---

## Steel Thread 7: An adapter depending on another adapter fails HEX-2

- [ ] **Task 7.1: `checkProjectDependencies` fails an adapter's project dependency on anything other than a domain-group project of its own subdomain or a shared library, naming HEX-2**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkProjectDependencies`
  - Observable: `implementation project(':a:a-persistence')` in `a-rest-api` fails with a message starting `HEX-2 (adapters depend only on the domain)` that names the dependency and configuration. `a-rest-api` depending on `a-domain` and `a-sagas` passes.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.Hex2AdaptersDependOnlyOnDomainTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [ ] Write `Hex2AdaptersDependOnlyOnDomainTest`, covering: an adapter on an adapter fails; an adapter on `domain`, `sagas` and a shared library passes; an adapter on `main` fails.
    - [ ] Create `Hex2AdaptersDependOnlyOnDomain` and add it to `CheckProjectDependenciesTask`. Add a comment that HEX-3 follows from HEX-1.
    - [ ] Run `./gradlew -p buildSrc check` and `./gradlew build`.

---

## Steel Thread 8: Main compiling against an adapter it does not use fails HEX-7

- [ ] **Task 8.1: `checkProjectDependencies` fails main's compile-scope dependency on an adapter that the source set does not reference, and a main class outside the root package, naming HEX-7**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkProjectDependencies`
  - Observable: `implementation project(':a:a-command-api')` in main, with no file in `src/main/java` mentioning the adapter's package or one of its `messageCopyPackages`, fails with a message starting `HEX-7 (main contains no business logic)` that names the dependency. `componentTestImplementation` on an adapter whose package appears in `src/componentTest/java` passes, and `runtimeOnly` passes. A class in a sub-package of the root package in main's `src/main/java` fails. The current `customer-service-main/build.gradle` passes, because `CustomerServiceComponentTest` references the `customermanagement.commandapi` package and the event-subscribers copy package `othersubdomain.domain`.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.Hex7MainContainsNoBusinessLogicTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [ ] Write `Hex7MainContainsNoBusinessLogicTest` with fixture source files in the main fixture's `src/main/java` and `src/componentTest/java`, covering each case in the Observable.
    - [ ] Create `Hex7MainContainsNoBusinessLogic`. For each adapter dependency of main in `implementation`, `api` or `<sourceSet>Implementation`, it scans the `.java` files of that source set for the adapter's package or one of its message copy packages. It also checks that `src/main/java` holds only files directly in the root package. Add it to `CheckProjectDependenciesTask`.
    - [ ] Record the source-scan approach in the HEX-7 bullet of `docs/design/architecture-rules-enforcement.md`.
    - [ ] Run `./gradlew -p buildSrc check` and `./gradlew build`.

---

## Steel Thread 9: A broker artifact outside main fails HEX-9

- [ ] **Task 9.1: `checkProjectDependencies` fails a message broker artifact declared by a non-main project outside its integration and component test source sets, naming HEX-9**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkProjectDependencies`
  - Observable: `implementation "io.eventuate.tram.core:eventuate-tram-spring-jdbc-kafka"` in a `sagas` project fails with a message starting `HEX-9 (the message broker is declared in main only)` that names the artifact and configuration. The same artifact in `integrationTestImplementation`, `integrationTestRuntimeOnly` and `componentTestImplementation` passes, and in main's `implementation` it passes.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.Hex9MessageBrokerInMainOnlyTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [ ] Write `Hex9MessageBrokerInMainOnlyTest`, covering each case in the Observable plus `testImplementation` in an adapter, which fails.
    - [ ] Create `MessageBrokerArtifacts`, an allow-list of group and artifact: `io.eventuate.tram.core:eventuate-tram-spring-jdbc-kafka`, `eventuate-tram-spring-consumer-kafka`, and their `activemq`, `rabbitmq` and `redis` equivalents.
    - [ ] Create `Hex9MessageBrokerInMainOnly`. It accepts any configuration of the `integrationTest` and `componentTest` source sets in non-main projects. Add it to `CheckProjectDependenciesTask`.
    - [ ] Record in the HEX-9 bullet of `docs/design/architecture-rules-enforcement.md` that every configuration of the `integrationTest` and `componentTest` source sets is accepted, because `customer-management-event-subscribers` declares the Kafka consumer `integrationTestRuntimeOnly`.
    - [ ] Run `./gradlew -p buildSrc check` and `./gradlew build`.

---

## Steel Thread 10: A second composition root fails SUB-7

- [ ] **Task 10.1: `checkProjectDependencies` fails a non-main project with dependencies on adapters of more than one subdomain, and any dependency on main, naming SUB-7**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkProjectDependencies`
  - Observable: A shared library that depends on adapter projects of `a` and `b` fails with a message starting `SUB-7 (main is the only composition root)`. A project depending on `x-main` fails with the same rule identifier. Main depending on adapters of `a` and `b` passes.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.Sub7MainIsOnlyCompositionRootTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [ ] Write `Sub7MainIsOnlyCompositionRootTest`, covering the three cases in the Observable.
    - [ ] Create `Sub7MainIsOnlyCompositionRoot` and add it to `CheckProjectDependenciesTask`.
    - [ ] Run `./gradlew -p buildSrc check` and `./gradlew build`.

---

## Steel Thread 11: API and shared-library projects with forbidden dependencies fail HEX-8 and SUB-8

- [ ] **Task 11.1: `checkProjectDependencies` fails an `api` or `api-facet` project depending on a non-API project or on a Spring, JPA or Eventuate infrastructure artifact, naming HEX-8**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkProjectDependencies`
  - Observable: `a-api` with `implementation project(':a:a-domain')` fails with a message starting `HEX-8 (API projects contain only interfaces and value types)`. `a-api` with `implementation "org.springframework:spring-context"` fails. `a-api-observer` depending on `a-api` passes, and `a-api` depending on `eventuate-examples-common-money-jakarta9` passes.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.Hex8ApiProjectDependenciesTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [ ] Write `Hex8ApiProjectDependenciesTest`, covering the cases in the Observable.
    - [ ] Create `Hex8ApiProjectDependencies`. In the main source set configurations it forbids external groups starting with `org.springframework`, `jakarta.persistence`, `org.hibernate`, `io.eventuate.tram`, `io.eventuate.common`, `io.eventuate.messaging` and `io.eventuate.platform`. Add it to `CheckProjectDependenciesTask`.
    - [ ] Record the forbidden group prefixes in the HEX-8 bullet of `docs/design/architecture-rules-enforcement.md`.
    - [ ] Run `./gradlew -p buildSrc check` and `./gradlew build`.

- [ ] **Task 11.2: `checkProjectDependencies` fails a `shared-library` project depending on any subdomain project or other shared library, naming SUB-8**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkProjectDependencies`
  - Observable: A shared library `money` with `implementation project(':a:a-api')` fails with a message starting `SUB-8 (shared libraries hold only value types)`. A shared library depending on another shared library fails. A shared library with only external dependencies passes.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.Sub8SharedLibraryProjectDependenciesTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [ ] Write `Sub8SharedLibraryProjectDependenciesTest`, covering the cases in the Observable.
    - [ ] Create `Sub8SharedLibraryProjectDependencies` and add it to `CheckProjectDependenciesTask`.
    - [ ] Run `./gradlew -p buildSrc check` and `./gradlew build`.

---

## Steel Thread 12: A cross-subdomain dependency from the wrong project fails SUB-3

- [ ] **Task 12.1: `checkProjectDependencies` fails a dependency on another subdomain's API from a project other than `domain` and `sagas`, and on `api-observer` from a project other than `domain`, naming SUB-3**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkProjectDependencies`
  - Observable: `a-event-subscribers` with `implementation project(':b:b-api')` fails with a message starting `SUB-3 (four interaction mechanisms)`. `a-sagas` on `b-api-observer` fails. `a-domain` on `b-api-observer` passes, and `a-sagas` on `b-api` passes.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.Sub3FourInteractionMechanismsTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [ ] Write `Sub3FourInteractionMechanismsTest`, covering the cases in the Observable.
    - [ ] Create `Sub3FourInteractionMechanisms` and add it to `CheckProjectDependenciesTask`. The `api-observer` facet is identified by the facet name `observer`.
    - [ ] Run `./gradlew -p buildSrc check` and `./gradlew build`.

---

## Steel Thread 13: A cycle between subdomains fails SUB-6 at the root

- [ ] **Task 13.1: The root `checkSubdomainGraph` fails on a cycle of API dependencies between subdomains, printing the cycle**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkSubdomainGraph`
  - Observable: With `a-domain` depending on `b-api` and `b-domain` depending on `a-api`, `checkSubdomainGraph` fails with a message starting `SUB-6 (no cycles between subdomains via shared code)` that contains the cycle `a -> b -> a`. The root `check` depends on `checkSubdomainGraph`, so `./gradlew build` runs it. `./gradlew checkProjectDependencies checkSubdomainGraph` runs no `compileJava` task.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.Sub6NoSubdomainCyclesTest` exits 0, `./test-scripts/test-end-to-end.sh` exits 0, and `./gradlew build` exits 0.
  - Steps:
    - [ ] Write `Sub6NoSubdomainCyclesTest`, covering the cycle case and an acyclic `a -> b` case.
    - [ ] When `ArchitectureRulesPlugin` is applied to the root project, it applies `base`, so that the root has `check`, and registers `CheckSubdomainGraphTask`. The task builds the graph of subdomains from every subproject's role, subdomain and project dependencies on `api` and `api-facet` projects, and fails on a cycle.
    - [ ] Add `apply plugin: 'io.eventuate.architecture.rules'` at the top level of `build.gradle`, outside `subprojects`, and wire the root `check` to `checkSubdomainGraph`.
    - [ ] Update `test-scripts/test-project-dependency-checks-do-not-compile.sh` to run `./gradlew checkProjectDependencies checkSubdomainGraph --dry-run` and assert that `:checkSubdomainGraph` is listed and no `compileJava` task is listed.
    - [ ] Record in the "Wiring" bullet of `docs/design/architecture-rules-enforcement.md` that the root build file also applies the common plugin to the root, to register `checkSubdomainGraph` and hold the exempt project list.
    - [ ] Run `./gradlew -p buildSrc check`, `./test-scripts/test-end-to-end.sh` and `./gradlew build`.

---

## Steel Thread 14: An API project with a configuration class fails HEX-6 in `checkArchitectureRules`

- [ ] **Task 14.1: The HEX-6 ArchUnit rule in the `architecture-rules` subproject rejects a `@Configuration` class in an API project and a `@Bean` returning another project's type**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew :architecture-rules:test`
  - Observable: The rule fails on a fixture `api` package containing a `@Configuration` class, with a message naming `HEX-6 (each project wires itself)` and the class. The rule fails on a fixture `domain` package whose `@Bean` method returns a type in a sibling project package under the root package. The rule passes on a fixture whose `@Bean` methods return a type in its own package, in a sub-package, and outside the root package.
  - Evidence: `./gradlew :architecture-rules:test --tests io.eventuate.architecture.rules.Hex6EachProjectWiresItselfTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [ ] Add `archunitVersion` to `gradle.properties`, set to the latest `com.tngtech.archunit:archunit-junit5` release on Maven Central.
    - [ ] Add `include 'architecture-rules'` to `settings.gradle`.
    - [ ] Create `architecture-rules/build.gradle`. It applies the plugins it needs, declares `implementation "com.tngtech.archunit:archunit-junit5:$archunitVersion"` and `implementation 'org.junit.jupiter:junit-jupiter'`, and adds `testImplementation 'org.springframework:spring-context'` for the fixtures.
    - [ ] Add `architectureRules { exemptProjects = [':architecture-rules']; rulesProject = ':architecture-rules' }` to the top level of `build.gradle`. Make `ArchitectureRulesPlugin` skip the role requirement for exempt projects. Extend `ProjectWithoutRoleTest` with an exempt-project case.
    - [ ] Write `Hex6EachProjectWiresItselfTest` with fixture classes under `architecture-rules/src/test/java/io/eventuate/architecture/rules/fixtures/hex6/`, covering each case in the Observable.
    - [ ] Create these classes in `architecture-rules/src/main/java/io/eventuate/architecture/rules/`:
      - `RoleContext`: a record holding the role, group, subdomain, package and classes directory, read from the `architectureRules.*` system properties. Its `rootPackage()` method derives the root package from the package and subdomain.
      - `ArchitectureRuleDefinitions`: a static factory per rule taking a `RoleContext` and returning an `ArchRule` described with its rule identifier and title. Start with `hex6EachProjectWiresItself`.
    - [ ] Run `./gradlew :architecture-rules:test`.

- [ ] **Task 14.2: `checkArchitectureRules` runs the ArchUnit rules against every project with a role after `classes`, and `check` depends on it**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkArchitectureRules`
  - Observable: Every project with a role has a `checkArchitectureRules` `Test` task that writes JUnit XML under `build/test-results/checkArchitectureRules`. Adding a `@Configuration` class to a project with role `api` fails the task with a message naming HEX-6 and the class. Every project in this repository passes. `test` runs after `checkArchitectureRules`. `architecture-rules` has no `checkArchitectureRules` task.
  - Evidence: `./gradlew -p buildSrc test --tests io.eventuate.architecture.rules.gradle.CheckArchitectureRulesTaskTest` exits 0 and `./gradlew build` exits 0, running `:customer-management:customer-management-domain:checkArchitectureRules` and the same task in every other project with a role.
  - Steps:
    - [ ] Write `CheckArchitectureRulesTaskTest` using `RuleFixtureBuild`. For each role it asserts that the task exists, that its system properties hold the role, group, subdomain, package and classes directory, that `check` depends on it, and that `test` must run after it. It asserts that an exempt project has no such task.
    - [ ] Create `ClassesDirLocationProvider`, which reads `architectureRules.classesDir`. Create `ArchitectureRulesTest` in `architecture-rules/src/main/java/io/eventuate/architecture/rules/`, annotated `@AnalyzeClasses(locations = ClassesDirLocationProvider.class)`, with an `@ArchTest` method that checks `hex6EachProjectWiresItself` for the role in `RoleContext`.
    - [ ] In `ArchitectureRulesPlugin`, for each project with a role:
      - Add two resolvable configurations on the rules project: `architectureRulesClasses`, with the `LibraryElements.CLASSES` attribute, and `architectureRulesRuntime`.
      - Register `checkArchitectureRules` as a `Test` task using JUnit Platform. Set `testClassesDirs` from `architectureRulesClasses`, and `classpath` to `architectureRulesRuntime` plus `sourceSets.main.output`. Set the five system properties.
      - Make `check` depend on it, and make `test` `mustRunAfter` it.
    - [ ] Run `./gradlew -p buildSrc check` and `./gradlew build`.

---

## Steel Thread 15: A domain class depending on a transitive infrastructure type fails HEX-4

- [ ] **Task 15.1: `checkArchitectureRules` fails a domain-group class that depends on a forbidden infrastructure package, naming HEX-4, the class and the forbidden class**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkArchitectureRules`
  - Observable: A fixture domain class with a field of type `org.springframework.web.client.RestClient` fails with a message naming `HEX-4 (domain projects stay free of infrastructure technology)`, the class and `org.springframework.web.client.RestClient`. A fixture sagas class depending on `io.eventuate.tram.commands.consumer.CommandWithDestination` passes, and the same class in a domain fixture fails. `customer-management-domain` and `customer-management-sagas` pass.
  - Evidence: `./gradlew :architecture-rules:test --tests io.eventuate.architecture.rules.Hex4DomainClassDependenciesTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [ ] Add `testImplementation 'org.springframework:spring-web'` and `testImplementation 'io.eventuate.tram.sagas:eventuate-tram-sagas-spring-orchestration-simple-dsl-starter'` to `architecture-rules/build.gradle` for the fixtures.
    - [ ] Write `Hex4DomainClassDependenciesTest` with fixtures under `fixtures/hex4/`, covering each case in the Observable.
    - [ ] Add `hex4DomainClassDependencies` to `ArchitectureRuleDefinitions`. The forbidden packages are `java.sql..`, `javax.sql..`, `org.springframework.web..`, `org.springframework.jdbc..`, `org.flywaydb..`, `jakarta.servlet..`, `org.apache.kafka..`, `io.eventuate.tram.messaging.producer..`, `io.eventuate.tram.messaging.consumer..` and `io.eventuate.tram.commands..`. For `sagas`, `io.eventuate.tram.commands.common..`, `io.eventuate.tram.commands.consumer..` and `io.eventuate.tram.sagas..` are permitted. Add an `@ArchTest` method to `ArchitectureRulesTest` that applies it to domain-group roles.
    - [ ] Record in the HEX-4 class dependencies bullet of `docs/design/architecture-rules-enforcement.md` that the forbidden packages are the ones listed above, and that `sagas` is also permitted the Eventuate Tram command `common` and `consumer` packages, which the orchestration DSL's `Command` and `CommandWithDestination` types require.
    - [ ] Run `./gradlew :architecture-rules:test` and `./gradlew build`.

---

## Steel Thread 16: An API or shared-library class with behaviour or framework annotations fails HEX-8 and SUB-8

- [ ] **Task 16.1: `checkArchitectureRules` fails a class in an `api` or `api-facet` project that is not an interface, record or enum, or that carries a Spring, JPA or Eventuate annotation, naming HEX-8**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkArchitectureRules`
  - Observable: In an `api` fixture, a plain class fails, and a record annotated `@jakarta.persistence.Embeddable` fails. Each message names `HEX-8 (API projects contain only interfaces and value types)` and the class. An interface, a record and an enum with no annotations pass.
  - Evidence: `./gradlew :architecture-rules:test --tests io.eventuate.architecture.rules.Hex8ApiContentsTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [ ] Add `testImplementation 'jakarta.persistence:jakarta.persistence-api'` to `architecture-rules/build.gradle`.
    - [ ] Write `Hex8ApiContentsTest` with fixtures under `fixtures/hex8/`.
    - [ ] Add `valueTypesOnly(Rule)` to `ArchitectureRuleDefinitions`. It checks that every class is an interface, a record or an enum, and that no class carries an annotation from `org.springframework..`, `jakarta.persistence..` or `io.eventuate..`. Add `hex8ApiContents`, which uses it, and an `@ArchTest` method for `api` and `api-facet`.
    - [ ] Run `./gradlew :architecture-rules:test` and `./gradlew build`.

- [ ] **Task 16.2: `checkArchitectureRules` fails a class in a `shared-library` project that is not a value type, naming SUB-8**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkArchitectureRules`
  - Observable: In a `shared-library` fixture, a plain class fails with a message naming `SUB-8 (shared libraries hold only value types)` and the class. A record passes.
  - Evidence: `./gradlew :architecture-rules:test --tests io.eventuate.architecture.rules.Sub8SharedLibraryContentsTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [ ] Write `Sub8SharedLibraryContentsTest` with fixtures under `fixtures/sub8/`.
    - [ ] Add `sub8SharedLibraryContents` to `ArchitectureRuleDefinitions`, using `valueTypesOnly`, and add an `@ArchTest` method for `shared-library`.
    - [ ] Run `./gradlew :architecture-rules:test` and `./gradlew build`.

---

## Steel Thread 17: A message copy carrying an Eventuate annotation fails SUB-4

- [ ] **Task 17.1: `checkArchitectureRules` fails a class outside the project's own package that carries an `io.eventuate` annotation, naming SUB-4**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew checkArchitectureRules`
  - Observable: In an `event-subscribers` fixture, a copy class in another package with a method annotated `@EventuateDomainEventHandler` fails, with a message naming `SUB-4 (messages are copied, not shared)` and the class. A copy class with no annotations passes. `customer-management-sagas` and `customer-management-event-subscribers` pass.
  - Evidence: `./gradlew :architecture-rules:test --tests io.eventuate.architecture.rules.Sub4MessageCopiesCarryNoEventuateAnnotationsTest` exits 0 and `./gradlew build` exits 0.
  - Steps:
    - [ ] Add `testImplementation 'io.eventuate.tram.core:eventuate-tram-spring-events-subscriber-starter'` to `architecture-rules/build.gradle`.
    - [ ] Write `Sub4MessageCopiesCarryNoEventuateAnnotationsTest` with fixtures under `fixtures/sub4/`.
    - [ ] Add `sub4MessageCopiesCarryNoEventuateAnnotations` to `ArchitectureRuleDefinitions`. It checks classes, methods and fields, and its scope is the classes outside `RoleContext.packageName()` and its sub-packages. Add an `@ArchTest` method that applies it to every role.
    - [ ] Run `./gradlew :architecture-rules:test` and `./gradlew build`.

---

## Steel Thread 18: A generated service with two subdomains is checked on its first build

- [ ] **Task 18.1: A copy of the template with a second subdomain runs all three check tasks in every project and passes**
  - TaskType: INFRA
  - Entrypoint: `./test-scripts/test-generated-service-is-checked.sh`
  - Observable: The script copies the template into `build/generated-service-test/`, excluding `.git`, `.gradle` and `build` directories. It adds a `billing` subdomain with `billing/billing-domain`, which applies `io.eventuate.architecture.roles.domain` and holds one class in `io.eventuate.customerservice.billing.domain`. It runs `./gradlew checkProjectDependencies checkSubdomainGraph checkArchitectureRules` in the copy. The run exits 0, and its output shows `checkProjectDependencies` and `checkArchitectureRules` executed for `:billing:billing-domain` and for every `customer-management` project.
  - Evidence: `./test-scripts/test-end-to-end.sh` exits 0, running `test-cleanup.sh`, `test-project-dependency-checks-do-not-compile.sh` and `test-generated-service-is-checked.sh`.
  - Steps:
    - [ ] Create `test-scripts/test-cleanup.sh`, which removes `build/generated-service-test/`.
    - [ ] Create `test-scripts/test-generated-service-is-checked.sh`. It copies the template with `rsync`, adds the `billing` subdomain and its `include` line to the copy's `settings.gradle`, runs the three tasks with `--console=plain`, and asserts the executed task lines.
    - [ ] Update `test-scripts/test-end-to-end.sh` to run `test-cleanup.sh` first and then the new script.
    - [ ] Run `./test-scripts/test-end-to-end.sh`.

- [ ] **Task 18.2: The repository builds under every enforced rule, with no project-specific names in the plugins and rules**
  - TaskType: OUTCOME
  - Entrypoint: `./gradlew build`
  - Observable: `./gradlew build` runs `checkProjectDependencies` and `checkArchitectureRules` in every project with a role, and `checkSubdomainGraph` at the root, and passes. `grep -r customer-management buildSrc/src/main architecture-rules/src/main` prints nothing. The rules document's current-state section records no violation, and its TODO section has no entries. `README.md` describes the three check tasks, the role plugins and `messageCopyPackages`.
  - Evidence: `./gradlew -p buildSrc check` exits 0, `./gradlew build` exits 0, and `./test-scripts/test-end-to-end.sh` exits 0.
  - Steps:
    - [ ] Run `grep -r customer-management buildSrc/src/main architecture-rules/src/main`. Remove any project-specific name it finds.
    - [ ] Extend the "Architecture rules" section of `README.md` with the three check tasks, how to read a failure, and `architectureRules { messageCopyPackages = [...] }`.
    - [ ] Add a sentence to the "Initial implementation" section of `docs/design/architecture-rules-enforcement.md` stating that the implementation is in place and naming the class and task names it uses.
    - [ ] Update the current-state section of `docs/design/architecture-rules.md` to state that the rules are now enforced by the build. Leave SUB-5 and the usage part of SUB-3 listed as not enforced.
    - [ ] Run `./gradlew -p buildSrc check`, `./gradlew build` and `./test-scripts/test-end-to-end.sh`.

---

## Change History

### 2026-10-22: Put each Observable on a single line

Tasks 1.2, 1.3, 3.1, 5.1, 6.1, 8.1, 9.1, 11.1, 12.1, 13.1, 14.1, 14.2, 15.1, 17.1 and 18.2 listed their Observable as nested bullets, and the validator did not see them. Each Observable is now one line with the same content. In Task 9.1, "integrationTestImplementation, integrationTestRuntimeOnly or componentTestImplementation passes" now reads "and", so it says that every one of those configurations passes.

### 2026-10-22 18:45 - mark-step-complete
Baseline ./gradlew build passed before any change

### 2026-10-22 18:45 - mark-step-complete
main's domain, command-api and sagas dependencies changed to runtimeOnly plus componentTestImplementation

### 2026-10-22 18:45 - mark-step-complete
componentTest and ./gradlew build pass after the change

### 2026-10-22 19:07 - mark-task-complete
A subdomain directory project such as customer-management, which exists only because settings.gradle includes its children, is not a module and needs no role; the check skips projects with child projects.
