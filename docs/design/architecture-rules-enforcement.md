# Architecture rules enforcement

This document describes how the rules in [architecture-rules.md](architecture-rules.md) are enforced in the Gradle build. The rules document is the specification; this document is the design of the checker. The alternatives that were considered are in "Discarded options" at the end.

## Approach

A Gradle plugin checks the project graph and runs ArchUnit rules on each project's classes. Each project's role is declared by a role plugin. Both checks are wired into `check`, so `./gradlew build` and CI run them.

The pieces:

- **The common plugin.** Applied to every subproject by the root build file's `subprojects` block, so no build file can opt out of it. After a project is evaluated it fails the build if the project has been given no role, unless the root build file names the project as exempt. For a project with a role it derives the subdomain from the project's parent directory, checks that the project name matches the role's naming pattern, and registers the two check tasks.
- **Role plugins.** One Gradle plugin per role. A project's build file applies the plugin for its role and nothing else about the rules. The role plugin sets the role on the common plugin and applies the role's build conventions.
- **Two rule engines.** Build model rules, meaning rules about project dependencies, scopes, source sets and source tree paths, run against Gradle's project model before compilation. Class rules, meaning rules about class dependencies, annotations and class shape, are ArchUnit rules run against the project's compiled classes.
- **Rules as functions of roles.** No rule names a project. Each rule takes a role, its group and its subdomain, so one rule set covers every subdomain.

Rationale:

1. **The build model rules need Gradle.** Most rules are about projects and scopes, and two of the three current violations are scope violations that no class-level tool can see: HEX-4 (domain projects stay free of infrastructure technology) in the sagas project and HEX-7 (main contains no business logic) in main. The checker must at least report these.
2. **The class rules need ArchUnit.** HEX-6 (each project wires itself), HEX-8 (API projects contain only interfaces and value types) and SUB-4 (messages are copied, not shared) are about annotations and class shape. ArchUnit expresses them in a few lines each. Writing a class scanner by hand would duplicate ArchUnit.
3. **Coverage by construction.** A project is checked because its build file applies a role plugin. The skeleton generator copies the template repository for each new service and the `customer-management` subdomain directory for each of its subdomains, and the copied build files carry their role plugin lines, so every generated project is checked. A project created by hand, such as a second proxy adapter, is checked only if its author applies a role plugin, and a project with no role fails the build, so the omission is a build error rather than an unchecked project.
4. **Shared rule identifiers.** Every failure names the rule identifier from the rules document, so a failure points at the specification.

## Two kinds of dependency

The rules involve two kinds of dependency. This document uses Gradle's term for the first and ArchUnit's term for the second.

- **Project dependency.** A `project(...)` entry in a build file, with a scope.
- **Class dependency.** One class depending on another, as recorded in the compiled class: field, parameter and return types, method and constructor calls, superclasses, interfaces, annotations and generic signatures. This is what ArchUnit's `dependOnClassesThat()` and the JDK's `jdeps` examine. The rules document says "imports a class" for the class dependency clause of HEX-1 (the domain does not depend on adapters), HEX-2 (adapters depend only on the domain) and SUB-1 (subdomains interact only through APIs). An `import` statement with no use leaves nothing in the compiled class, so it is not a class dependency.

A class can depend only on classes on its project's compile classpath, which the project dependencies define. So a domain class can depend on an adapter class only if the domain project's build file declares a project dependency on the adapter project. Checking the project dependencies therefore enforces the class dependency clauses of HEX-1 (the domain does not depend on adapters), HEX-2 (adapters depend only on the domain) and SUB-1 (subdomains interact only through APIs) as well. ArchUnit is needed for what the classpath permits: types a permitted dependency brings transitively, annotations, and the shape of classes.

## Roles

Every rule is stated over roles, not over named projects: HEX-1 (the domain does not depend on adapters) says "the domain does not depend on adapters", not "`customer-management-domain` does not depend on `customer-management-persistence`". The roles come from the rules document's vocabulary section. Each has a name, a group, a naming pattern for the project, a package pattern, which HEX-5 (one project, one package) requires, and a role plugin.

| Role | Group | Project name | Package under `<root>` | Role plugin |
|---|---|---|---|---|
| `api` | domain | `<subdomain>-api` | `<subdomain>.api` | `io.eventuate.architecture.roles.api` |
| `api-facet` | domain | `<subdomain>-api-<facet>` | `<subdomain>.api.<facet>` | `io.eventuate.architecture.roles.api-facet` |
| `domain` | domain | `<subdomain>-domain` | `<subdomain>.domain` | `io.eventuate.architecture.roles.domain` |
| `sagas` | domain | `<subdomain>-sagas` | `<subdomain>.sagas` | `io.eventuate.architecture.roles.sagas` |
| `rest-api` | adapter, inbound | `<subdomain>-rest-api` | `<subdomain>.restapi` | `io.eventuate.architecture.roles.rest-api` |
| `command-api` | adapter, inbound | `<subdomain>-command-api` | `<subdomain>.commandapi` | `io.eventuate.architecture.roles.command-api` |
| `event-subscribers` | adapter, inbound | `<subdomain>-event-subscribers` | `<subdomain>.eventsubscribers` | `io.eventuate.architecture.roles.event-subscribers` |
| `persistence` | adapter, outbound | `<subdomain>-persistence` | `<subdomain>.persistence` | `io.eventuate.architecture.roles.persistence` |
| `event-publishing` | adapter, outbound | `<subdomain>-event-publishing` | `<subdomain>.eventpublishing` | `io.eventuate.architecture.roles.event-publishing` |
| `proxy` | adapter, outbound | `<subdomain>-<other>-proxy` | `<subdomain>.<other>proxy` | `io.eventuate.architecture.roles.proxy` |
| `main` | main | `<service>-main` | `<root>` | `io.eventuate.architecture.roles.main` |
| `shared-library` | shared library | `<name>` | `<name>` | `io.eventuate.architecture.roles.shared-library` |

The subdomain is the project's parent directory, and `<subdomain>` in a package is the directory name with hyphens removed, so `customer-management` is `customermanagement`. Most rules use the group. A few use the role:

- The permitted list of HEX-4 (domain projects stay free of infrastructure technology) differs between `domain` and `sagas`.
- HEX-8 (API projects contain only interfaces and value types) applies to `api` and `api-facet`.
- HEX-7 (main contains no business logic) and SUB-7 (main is the only composition root) apply to `main`.
- SUB-8 (shared libraries hold only value types) applies to `shared-library`.
- SUB-3 (four interaction mechanisms) applies to `domain`, `sagas` and `event-subscribers`.
- SUB-5 (each subdomain owns its data) applies to `persistence`.

### How a project gets its role

Each build file applies the role plugin for its role:

```groovy
plugins {
    id 'io.eventuate.architecture.roles.domain'
}
```

The role plugin sets that role on the common plugin, which the root build file has already applied to every subproject. After the project is evaluated, the common plugin fails the build if no role was set, so a project cannot go unchecked by omitting the line. It also checks that the project name matches the role's pattern, so the naming convention in the rules document is a checked rule rather than the source of the role.

A role plugin is also where the role's build conventions go. The role plugins for the adapter roles and `sagas` apply the integration-tests plugin, and `main` applies `ServicePlugin`, which is already a role plugin for main in all but name, and the component-tests plugin. The Spring Cloud Contract plugins stay in the build files: whether a project owns or consumes contracts depends on the project, not its role. Build files shrink to the role plugin line, any contract plugin lines, and the project's own dependencies.

ArchUnit cannot read a build file, so the common plugin passes the role, the group, the subdomain and the package to the ArchUnit rules when it runs them. The rules never discover the role themselves.

The `architecture-rules` subproject that holds the ArchUnit rules (see "Initial implementation") is not part of the service and has no role. The root build file exempts it by name. It is the only exempt project.

## Rules implementation

Most rules are implemented in one engine. HEX-4 (domain projects stay free of infrastructure technology), HEX-8 (API projects contain only interfaces and value types) and SUB-8 (shared libraries hold only value types) have a project dependency clause in the plugin and a class clause in ArchUnit. The rules for HEX-1 (the domain does not depend on adapters), HEX-2 (adapters depend only on the domain), HEX-3 (outbound dependencies go through ports) and SUB-1 (subdomains interact only through APIs) are implemented in the plugin only: their class dependency clauses can be violated only after a forbidden project dependency is declared, and the plugin reports that before compilation.

| Rule | Engine |
|---|---|
| HEX-1 The domain does not depend on adapters | Gradle plugin |
| HEX-2 Adapters depend only on the domain | Gradle plugin |
| HEX-3 Outbound dependencies go through ports | Gradle plugin, follows from HEX-1 |
| HEX-4 Domain projects stay free of transport and infrastructure technology | Gradle plugin (dependency list) and ArchUnit (class dependencies) |
| HEX-5 One project, one package | Gradle plugin |
| HEX-6 Each project wires itself | ArchUnit |
| HEX-7 Main contains no business logic | Gradle plugin |
| HEX-8 API projects contain only interfaces and value types | Gradle plugin (project dependencies) and ArchUnit (contents) |
| HEX-9 The message broker is declared in main only | Gradle plugin |
| SUB-1 Subdomains interact only through APIs | Gradle plugin, follows from SUB-2 |
| SUB-2 Gradle dependencies between subdomains are on API projects only | Gradle plugin |
| SUB-3 Four interaction mechanisms | Gradle plugin, dependency shape only |
| SUB-4 Messages are copied, not shared | ArchUnit (annotations), contract tests (compatibility) |
| SUB-5 Each subdomain owns its data | not enforced |
| SUB-6 No cycles between subdomains via shared code | Gradle plugin |
| SUB-7 Main is the only composition root | Gradle plugin |
| SUB-8 Shared libraries hold only value types | Gradle plugin (project dependencies) and ArchUnit (contents) |

### Rules implemented by the Gradle plugin

These run in `checkProjectDependencies`, one task per project with no inputs other than the build scripts and the source tree, and in `checkSubdomainGraph`, one task at the root. Neither needs compilation, so a violation in a build file is reported in seconds. "In any configuration" below means every source set's scopes, including test, integration test, component test and contract test.

- **HEX-1 (the domain does not depend on adapters).** A project in group domain declares no project dependency, in any configuration, on a project in group adapter.
- **HEX-2 (adapters depend only on the domain).** A project in group adapter declares project dependencies only on projects in group domain of its own subdomain, on shared libraries, and on external libraries. A project dependency on another adapter of the same subdomain fails.
- **HEX-3 (outbound dependencies go through ports).** Follows from HEX-1 (the domain does not depend on adapters): a domain project cannot name an adapter's implementing class without a project dependency on the adapter.
- **HEX-4 (domain projects stay free of infrastructure technology), dependency list.** A project in group domain declares external dependencies, in the main source set configurations `api`, `implementation`, `compileOnly` and `runtimeOnly`, only from the permitted categories: Spring context, JPA and Spring Data, the Eventuate events API, the Money value type, and, for `sagas` only, the Eventuate Sagas orchestration DSL and the Eventuate Flyway migration support. Test source set configurations are not checked (decision 4). The categories are allow-lists of group and artifact in the plugin. Anything else fails, naming the dependency and the permitted categories for the project's role.
- **HEX-5 (one project, one package).** Every source file under `src/main/java` is in the project's package or a sub-package of it, by path. The SUB-4 (messages are copied, not shared) copy packages are the exception, declared per project in the build file as `architectureRules { messageCopyPackages = ['io.eventuate.otherservice.othersubdomain.domain'] }`.
- **HEX-7 (main contains no business logic).** Main's project dependencies on adapter projects are `runtimeOnly`, unless main's own source set or one of its test source sets has a class that depends on the adapter, in which case `implementation` or `<sourceSet>Implementation` is accepted. Main's `src/main/java` contains only classes in the root package.
- **HEX-8 (API projects contain only interfaces and value types), project dependencies.** A project with role `api` or `api-facet` declares project dependencies only on API projects of its own subdomain and on shared libraries, and no external dependency from Spring, JPA or Eventuate.
- **HEX-9 (the message broker is declared in main only).** Only `main` declares a message broker artifact in a main source set configuration. Any other project declares one only in `integrationTestImplementation` or `componentTestImplementation`. The broker artifacts are an allow-list of group and artifact in the plugin, as for HEX-4 (domain projects stay free of infrastructure technology).
- **SUB-1 (subdomains interact only through APIs).** Follows from SUB-2 (Gradle dependencies between subdomains are on API projects only).
- **SUB-2 (Gradle dependencies between subdomains are on API projects only).** A project in subdomain A declares a project dependency, in any configuration, on a project in subdomain B only if that project has role `api` or `api-facet`. `testImplementation project(':b:b-domain')` fails.
- **SUB-3 (four interaction mechanisms).** The dependency shape of the four mechanisms: only `domain` and `sagas` projects of A declare project dependencies on `api` and `api-facet` projects of B, and only `domain` projects of A declare a project dependency on B's `api-observer` facet. `event-subscribers` projects declare no project dependency on another subdomain. What a class does with the interface it depends on is not checked.
- **SUB-6 (no cycles between subdomains via shared code).** `checkSubdomainGraph` builds the graph whose nodes are subdomains and whose edges are project dependencies on `api` and `api-facet` projects, and fails on a cycle, printing it.
- **SUB-7 (main is the only composition root).** Only `main` declares project dependencies on adapter projects of more than one subdomain. No project declares a project dependency on `main`.
- **SUB-8 (shared libraries hold only value types).** A project with role `shared-library` declares no project dependency on any subdomain project or other shared library.
- **Naming.** The project name matches its role's pattern from the roles table, and the parent directory is a subdomain directory for every role in groups domain and adapter.

### Rules implemented by ArchUnit

These run in `checkArchitectureRules`, one task per project after `classes`, against the project's own `build/classes/java/main`. The rules are ArchUnit rules in the `architecture-rules` subproject, parameterised by the role, group, subdomain and package the plugin passes in. The task is a Gradle `Test` task whose test classes come from that subproject. The rules run under JUnit, read the role from a system property, and produce a standard JUnit report per project. The project itself contains no test class.

- **HEX-4 (domain projects stay free of infrastructure technology), class dependencies.** No class in a project of group domain depends on a class in `java.sql`, `javax.sql`, `org.springframework.web`, `org.springframework.jdbc`, `org.flywaydb`, `jakarta.servlet`, `org.apache.kafka`, or the Eventuate Tram producer, consumer and command packages. For `sagas` the Eventuate Sagas orchestration packages are permitted. This catches a type that a permitted starter such as Spring Data JPA brings transitively, which the dependency list cannot.
- **HEX-6 (each project wires itself).** Every method annotated `@Bean` in the project returns a type in the project's package, a sub-package of it, or a package outside the root package. A project with role `api` or `api-facet` has no class annotated `@Configuration`.
- **HEX-8 (API projects contain only interfaces and value types), contents.** Every class in a project with role `api` or `api-facet` is an interface, a record or an enum, and no class carries an annotation from `org.springframework`, `jakarta.persistence` or `io.eventuate`.
- **SUB-4 (messages are copied, not shared), annotations.** Every class in a package outside the project's own package, which is to say in a copy package, carries no annotation from `io.eventuate`. The compatibility of the copy with the producer is verified by the existing contract tests, not here.
- **SUB-8 (shared libraries hold only value types), contents.** As HEX-8 (API projects contain only interfaces and value types), for `shared-library`.

### Not enforced

- **SUB-5 (each subdomain owns its data).** Checking that migrations create only the subdomain's tables and that no query touches another subdomain's tables needs SQL parsing. With one subdomain there is nothing to check. Revisit when a second subdomain exists; a cheap partial check is that each Flyway migration file in a `persistence` project names only tables with the subdomain's prefix.
- **SUB-3 (four interaction mechanisms), how a mechanism is used.** The plugin checks which project of A depends on which API project of B. Whether A's domain calls the interface, or implements the observer and registers it, is not a rule a tool can check.
- **`other-service`.** A separate build delivered by `includeBuild`, outside the rules by the rules document's own statement. The plugins are not applied to it.

No known-violations list is provided, neither `FreezingArchRule` nor an equivalent for the plugin checks. Before enabling the plugins, resolve the HEX-7 (main contains no business logic) TODO in the rules document so the checks pass on the first run.

## Initial implementation

The first implementation lives in this project.

- **Plugins in `buildSrc`.** The common plugin, `ArchitectureRulesPlugin`, and one role plugin class per row of the roles table, next to `ServicePlugin` and the Docker plugins. `buildSrc/build.gradle` applies `java-gradle-plugin` and declares the plugin ids from the roles table so that build files can use the `plugins { id '...' }` form. The role table, the HEX-4 (domain projects stay free of infrastructure technology) allow-lists and the project dependency rules are code and data in `buildSrc/src/main/groovy`, with tests in `buildSrc/src/test`, one per rule identifier, using Gradle's `ProjectBuilder`.
- **ArchUnit rules in a subproject.** A new `architecture-rules` subproject holds the ArchUnit rules.
  - It is a `java-library` with ArchUnit and JUnit Jupiter as `implementation` dependencies. Its own tests run the rules against small fixture classes, one per rule identifier.
  - The rules are a JUnit test class. `@AnalyzeClasses` uses a location provider that reads the project's classes directory from a system property, and the `@ArchTest` rules read the role, group, subdomain and package from others.
  - `buildSrc` cannot depend on a subproject, so the common plugin does not call the rules directly. It registers `checkArchitectureRules` as a `Test` task with `testClassesDirs` set to the subproject's output, `classpath` set to the subproject's runtime classpath plus the project's main output, and the role, group, subdomain, package and classes directory as system properties.
- **Wiring.** `settings.gradle` includes `architecture-rules`. The root build file applies `ArchitectureRulesPlugin` in its `subprojects` block and names `architecture-rules` as exempt. Each existing build file replaces `apply plugin` lines and per-role boilerplate with its role plugin. `check` in every project with a role depends on `checkProjectDependencies` and `checkArchitectureRules`; `checkSubdomainGraph` is attached to the root `check`. `compileAll` is unchanged.
- **Order of work.** Resolve the HEX-7 (main contains no business logic) TODO in the rules document; add the role plugins and the common plugin with the project dependency rules; convert the build files; add the `architecture-rules` subproject and the `checkArchitectureRules` task; run `./gradlew build`.

Once the rules are stable and a second service needs them, the plugins and the rules subproject move to the Eventuate plugin repository and are published as `io.eventuate.plugins.gradle.architecture-rules`, alongside `io.eventuate.plugins.gradle.testing.*`, so that services built from the template get rule updates by changing a version. Until then the skeleton generator copies `buildSrc` and `architecture-rules` with the rest of the template.

## Decisions

1. **A project whose name does not match its declared role's pattern fails the build.** The naming convention is a rule. A warning would let names drift from roles.
2. **The permitted list of HEX-4 (domain projects stay free of infrastructure technology) is data in the plugin.** Every service built from the template has the same list, and adding a category is a plugin change, which matches the rule's last sentence. Per-build configuration would let a service extend the list, which the rule does not allow.
3. **The role plugins carry build conventions from the first version.** The role plugins for the adapter roles and `sagas` apply the integration-tests plugin, and `main` applies `ServicePlugin` and the component-tests plugin. The contract plugins stay in the build files, because which projects own or consume contracts is not a function of the role. This couples the rules plugin to the test plugins, in `buildSrc` as a dependency on the test plugin artifacts and, once published, as a pinned test plugin version that every service applying a role plugin gets. Every service built from the template uses the test plugins, so the coupling costs nothing. `ServicePlugin` already applies the Spring Boot plugin the same way.
4. **The dependency list of HEX-4 (domain projects stay free of infrastructure technology) applies to the main source set configurations only.** These are `api`, `implementation`, `compileOnly` and `runtimeOnly`. Test source sets do not ship, and HEX-9 (the message broker is declared in main only) places broker artifacts in integration test configurations, so checking them would fail every domain project that has integration tests.

## Discarded options

Alternatives that were considered and set aside, each with its reasons. Enforcement mechanisms come first, then ways of assigning a role.

### Gradle plugin checking the project graph, on its own

Discarded because it covers none of the class content rules except HEX-5 (one project, one package). It is half of the chosen approach.

- Pros:
  - The rules are stated in terms of projects and scopes, and this is the only mechanism that sees projects and scopes.
  - Runs without compiling. A violation in a build file is reported in seconds, before any test.
- Cons:
  - Cannot see class dependencies, annotations or class shape.

### ArchUnit tests, on their own

Discarded because ArchUnit cannot see Gradle scopes or build files, so on its own it would report the current `customer-management-sagas` state as compliant and cannot check HEX-7 (main contains no business logic) at all. It is the other half of the chosen approach.

[ArchUnit](https://www.archunit.org) reads compiled class files, never source, and evaluates rules written in Java against the class dependencies they record: `noClasses().that().resideInAPackage("..domain..").should().dependOnClassesThat().resideInAnyPackage("java.sql..", "org.springframework.web..")`. Two placements are possible: one test in `customer-service-main`, which has every project on its test runtime classpath, or one test per project loading its own `build/classes/java/main`.

- Pros:
  - Expresses class content rules directly, in Java, with rule text that reads like the specification.
  - Widely known. Failures list the offending classes and the dependency that violates the rule.
  - Runs in the IDE as a test.
- Cons:
  - Cannot see Gradle scopes or build files. The `runtimeOnly` distinction in HEX-7 (main contains no business logic) and the `runtimeOnly` Flyway dependency under HEX-4 (domain projects stay free of infrastructure technology) are invisible to it.
  - A domain class can depend on an adapter class only if the domain project's build file already declares a project dependency on the adapter project. So for HEX-1 (the domain does not depend on adapters), HEX-2 (adapters depend only on the domain) and SUB-1 (subdomains interact only through APIs), every violation ArchUnit can find is preceded by a build file violation that a plugin reports before compilation.
  - The per-project placement needs a test class in every project. A project that lacks one is silently unchecked.
  - The single-test-in-main placement reports every violation in main's test run, away from the project that caused it, and cannot check HEX-5 (one project, one package) cleanly, because ArchUnit sees packages, not projects.

### Gradle plugin checking the project graph, with hand-written ArchUnit tests in each project

Discarded because it needs a test class per project, which a copier can omit, and a second path for the role to reach the test.

The plugin checks the project graph and supplies the role. Each project has its own ArchUnit test class in `src/test`, run by the ordinary `test` task, which reads the role from a system property the plugin sets or derives it from the package name, and runs the shared rules for that role.

- Pros:
  - The class-level checks are ordinary JUnit tests in the project: JUnit reports, IDE test runner, `FreezingArchRule` and the rest of ArchUnit's JUnit support.
  - A rule change is a change to a test-support library, not a plugin rebuild.
- Cons:
  - A test class per project, which a copier can omit. The plugin can check that every project with a role has one, but that is one more check to maintain.
  - The role reaches the test through a system property or the package name, a second path beside the one the plugin uses for the project graph checks.
  - Two places to look when a rule fails: the plugin's task for project dependencies, the test report for classes.

### Gradle resolution hooks instead of a plugin

Discarded because it cannot express project-to-project rules, scopes or cycles, and a violation surfaces as a lazy resolution failure rather than in a named task.

The root build file installs `resolutionStrategy.eachDependency` on every configuration of domain projects and throws when a forbidden group appears. No plugin, a few lines in `build.gradle`.

- Pros:
  - The smallest possible change for the dependency list of HEX-4 (domain projects stay free of infrastructure technology).
- Cons:
  - Resolution runs lazily, so the violation surfaces when the offending project compiles, not in a named task, and the message is a resolution failure.
  - Cannot express project-to-project rules, scope rules or cycles without growing into a plugin with worse structure.
  - `exclude` rules, the tempting alternative, silently remove the dependency rather than failing, which hides the violation.

### Dependency Analysis Gradle Plugin

Discarded because it is a dependency hygiene tool, not a rule checker, and Spring auto-configuration starters make its report noisy. It can still be run on demand as an advisory report.

The [dependency-analysis-gradle-plugin](https://github.com/autonomousapps/dependency-analysis-gradle-plugin) reports unused dependencies and wrong scopes from bytecode analysis. It would report main's three `implementation` dependencies as unused, which is the HEX-7 (main contains no business logic) violation, and the unused Flyway dependencies in the sagas project.

- Pros:
  - An existing tool, no code, and it finds misdeclared scopes everywhere, not only where a rule names them.
- Cons:
  - It cannot say that a domain project must not depend on an adapter project.
  - Spring auto-configuration starters are used by the runtime, not by the code, so it reports them as unused unless each is listed as an exception. The noise would be large in this build.
  - Adds analysis time to every build.

### Checkstyle `ImportControl`

Discarded because it checks only `import` statements, which the compiler already constrains once project dependencies are checked, and cannot check class shape, annotations or `@Bean` return types.

An XML file lists, per package, the packages that may be imported. The Checkstyle plugin parses the `import` statements in the source, without compilation, so it is the one option that checks imports in the literal sense, unused ones included.

- Pros:
  - No compilation; standard plugin; IDE support.
- Cons:
  - Covers only the class dependency clauses, most of which follow from the project dependencies once those are checked. It cannot check class shape, annotations or `@Bean` return types.
  - The XML grows with every package and every exception, and it is package-based, so it has the same project-to-package indirection as ArchUnit without ArchUnit's expressiveness.

### Java Platform Module System

Discarded because the in-service message copies of SUB-4 (messages are copied, not shared) are split packages, which JPMS forbids, and because the Spring Boot jar runs on the classpath so JPMS would enforce at compile time only.

Each project declares `module-info.java` with `requires` and `exports`. The compiler enforces the dependency graph and HEX-5 (one project, one package).

- Pros:
  - Enforced by the compiler, with no third-party tool.
- Cons:
  - SUB-4 (messages are copied, not shared) puts the consumer's copy of a message class in the producer's package. When producer and consumer are in the same service, as with `ReserveCreditCommand` in `customer-management-command-api` and `customer-management-sagas`, that is a split package, which JPMS forbids. The rule and the mechanism are incompatible.
  - Spring, Hibernate and the Eventuate libraries need `opens` for reflection, and the Eventuate artifacts are automatic modules at best.
  - The Spring Boot jar runs on the classpath, so JPMS enforces at compile time only.

### Spring Modulith

Discarded because its one-package-per-module model does not match a subdomain whose API is a set of Gradle projects, and its event publication registry overlaps the Eventuate Tram outbox.

[Spring Modulith](https://spring.io/projects/spring-modulith) derives application modules from the direct sub-packages of the application package and verifies, with ArchUnit underneath, that modules use only each other's API packages.

- Pros:
  - A supported Spring project with documentation generation.
- Cons:
  - Its module model is one package per module with the top-level package as the API. Here a subdomain's API is a set of Gradle projects, its hexagonal structure is a set of sibling packages inside the module, and its message classes are copied into another subdomain's package. The model does not match, and the hexagonal rules inside a module are outside its scope.
  - Its event publication registry overlaps the Eventuate Tram outbox and would have to be disabled.

### jQAssistant

Discarded because a Neo4j database per run, a Cypher rule language and Maven-first tooling are too heavy for a service template.

[jQAssistant](https://jqassistant.org) scans compiled classes, jars, build descriptors and configuration files into an embedded Neo4j graph database, where a class, a method call, an artifact and a package containment are each a node or a relationship. Rules are Cypher queries: concepts label nodes, for example marking every class under a `.domain` package as a domain class, and constraints return the violations, for example every domain class that depends on a class in an adapter artifact. A constraint with a non-empty result fails the build. It runs under Maven, from the command line, or through a third-party Gradle plugin, and produces HTML reports.

- Pros:
  - Can express everything, including cross-project rules, if the Gradle model is scanned.
- Cons:
  - Maven-first with weaker Gradle support, a database per run, and a query language the team does not otherwise use.

### jMolecules

Discarded as a mechanism because it is an add-on to ArchUnit, and the hexagonal rules it supplies cover clauses the project dependency checks already report. Its DDD rules are worth revisiting if the rules document gains rules about aggregates and value objects.

[jMolecules](https://jmolecules.org) is a set of annotations and marker interfaces for expressing DDD and architecture concepts in code, with integrations that act on them. The DDD types are `@Entity`, `@AggregateRoot`, `@ValueObject`, `@Identity`, `@Repository`, `@Service` and `@DomainEvent`, or interfaces such as `AggregateRoot<T, ID>`. The architecture annotations include a hexagonal set, `@Port`, `@PrimaryPort`, `@SecondaryPort`, `@Adapter`, `@PrimaryAdapter`, `@SecondaryAdapter` and `@Application`, which go on classes or on `package-info.java`. The `jmolecules-archunit` module ships ready-made ArchUnit rules: `JMoleculesArchitectureRules.ensureHexagonal()` checks that application and domain code does not depend on adapters, that adapters do not depend on each other and that ports are interfaces; `JMoleculesDddRules.all()` checks that aggregates reference other aggregates only by identifier or `Association` and that value objects are immutable. A ByteBuddy plugin can translate the DDD annotations into JPA and Spring annotations, and Spring Modulith uses them for documentation.

- Pros:
  - The hexagonal rules come ready-made and maintained, with the same ArchUnit failure output as hand-written rules.
  - The DDD rules have no overlap with the rules document: `Customer` as `@AggregateRoot` and `CustomerId` as `@ValueObject` would gain aggregate-reference and immutability checks that no current rule states.
- Cons:
  - The hexagonal rules it provides are HEX-1 (the domain does not depend on adapters), HEX-2 (adapters depend only on the domain) and HEX-3 (outbound dependencies go through ports), whose class dependency clauses follow from the project dependencies that the plugin checks. It adds nothing for the dependency list of HEX-4 (domain projects stay free of infrastructure technology), HEX-6 (each project wires itself), HEX-7 (main contains no business logic), HEX-8 (API projects contain only interfaces and value types) or any SUB rule, and it cannot see Gradle scopes.
  - It classifies by annotation, while the rules classify by role and package. The `package-info.java` role would be a second statement of what the role plugin says, and the two can drift.
  - The annotation jar becomes a dependency of every domain and API project, so the permitted list of HEX-4 (domain projects stay free of infrastructure technology) and the "no annotations" clause of HEX-8 (API projects contain only interfaces and value types) both need an entry for it.

### Role from a package annotation

Discarded because the role is a property of the project and the project dependency rules run before compilation, where a package annotation is invisible.

A `package-info.java` in each project carries an annotation, jMolecules' or this template's own, naming the role.

- Pros:
  - The role is in the code, next to the classes it describes, and ArchUnit reads it directly with no plumbing from the build.
  - The same mechanism jMolecules uses, so its ready-made rules apply unchanged.
- Cons:
  - Visible only after compilation and only to a class-level checker. The project dependency rules, which are most of the rules, read build files before compilation and cannot use it.
  - Wrong level: the role is a property of the project, and the package follows from the project under HEX-5 (one project, one package).

### Role derived from the project name

Discarded because the role is implicit in a string and a project outside the convention has no role. The naming convention is kept as a rule the common plugin checks against the declared role.

The rules document's project tables fix the names. The plugin matches the name against those patterns and takes the subdomain from the parent directory.

- Pros:
  - Nothing to declare. Every existing project already follows the convention, and a project created by copying the template is classified on its first build.
  - The name and the role cannot disagree, because the name is the only source.
- Cons:
  - A project outside the convention has no role. The plugin must fail it or skip it, and skipping lets a project escape the rules by its name.
  - The `-api-<facet>` and `-<other>-proxy` patterns are open-ended, so the matcher orders patterns and the role of an unusual name is a matter of which pattern wins.
  - The role is implicit in a string. Reading a build file does not say what the project is.

### Roles declared in one central place

Discarded because the role lives away from the project it describes and duplicates what the name and directory say.

A block in the root build file or a settings plugin maps every project path to a role, next to the `include` lines.

- Pros:
  - The whole structure is visible in one place, and the subdomain graph for SUB-6 (no cycles between subdomains via shared code) is read directly.
- Cons:
  - The role lives away from the project it describes, and every new project is two edits, `include` and the role entry, that can drift apart.
  - Duplicates what the project name and directory already say.
