# Spec: Architecture rules enforcement

A Platform Capability Specification. The rules are specified in [docs/design/architecture-rules.md](../../../design/architecture-rules.md) and the checker is designed in [docs/design/architecture-rules-enforcement.md](../../../design/architecture-rules-enforcement.md). This specification states what the implementation must provide and points to those two documents for the detail. Where the two disagree with this specification, this specification is wrong.

## Purpose and context

The template's architecture rules HEX-1 to HEX-9 and SUB-1 to SUB-8 are enforced by nothing. This capability makes `./gradlew build` fail on a violation of any rule that the enforcement document's "Rules implementation" table marks as enforced, so that the rules hold in this repository and in every service the skeleton generator produces from it.

## Consumers

- **Project build files** in this repository. Each applies one role plugin and is otherwise unchanged except where the role plugin now supplies what the build file used to declare.
- **Developers** adding, renaming or rewiring a project. They get a build failure that names the rule identifier and the offending dependency, class or file.
- **The skeleton generator** (`docs/ideas/active/skeleton-generator`). It copies `buildSrc`, `architecture-rules` and the converted build files with the rest of the template, so a generated service is checked without further work.
- **Later, other services**, when the plugins are published as `io.eventuate.plugins.gradle.architecture-rules`. Out of scope here, but nothing in the implementation may depend on this repository's project names.

## Capabilities and behaviors

### Role declaration

- A project's build file declares its role with one line, `plugins { id 'io.eventuate.architecture.roles.<role>' }`, using the ids in the enforcement document's roles table.
- The common plugin is applied to every subproject by the root build file's `subprojects` block. After a project is evaluated, the build fails if no role was set, unless the project is `architecture-rules`, which the root build file names as exempt.
- The common plugin derives the subdomain from the project's parent directory and fails the build if the project name does not match its role's pattern from the roles table, or if a project in group domain or adapter is not in a subdomain directory.
- Each role plugin applies the role's build conventions: the role plugins for the six adapter roles and for `sagas` apply `io.eventuate.plugins.gradle.testing.integration-tests`, and `main` applies `ServicePlugin` and `io.eventuate.plugins.gradle.testing.component-tests`. The Spring Cloud Contract plugin and `PublishStubsPlugin` stay in the build files of the projects that own or consume contracts, today `rest-api`, `event-publishing` and `sagas`, because that depends on the project, not the role.

### Project dependency rules

The common plugin registers `checkProjectDependencies` in every project with a role and `checkSubdomainGraph` in the root project. They read build scripts and the source tree only and run without compilation. They implement the rules listed under "Rules implemented by the Gradle plugin" in the enforcement document: HEX-1, HEX-2, HEX-3, HEX-4 (dependency list), HEX-5, HEX-7, HEX-8 (project dependencies), HEX-9, SUB-1, SUB-2, SUB-3 (dependency shape), SUB-6, SUB-7, SUB-8 (project dependencies), and the naming check. The permitted dependency categories of HEX-4 (domain projects stay free of infrastructure technology) are allow-lists of group and artifact in the plugin, not configuration. The copy packages of HEX-5 (one project, one package) are declared per project as `architectureRules { messageCopyPackages = [...] }`.

### Class rules

The common plugin registers `checkArchitectureRules` in every project with a role, a Gradle `Test` task that runs the ArchUnit rules in the `architecture-rules` subproject against the project's `build/classes/java/main`. It implements the rules listed under "Rules implemented by ArchUnit": HEX-4 (class dependencies), HEX-6, HEX-8 (contents), SUB-4 (annotations) and SUB-8 (contents). The task passes the role, group, subdomain, package and classes directory as system properties. The project itself contains no test class for the rules.

### Failure reporting

Every failure names the rule identifier from the rules document and the offending item: the project dependency and its configuration, the external dependency and the category it would need, the class and the forbidden class it depends on, or the source file outside the project's package.

## Contracts and integration points

- **Role plugin ids.** `io.eventuate.architecture.roles.api`, `.api-facet`, `.domain`, `.sagas`, `.rest-api`, `.command-api`, `.event-subscribers`, `.persistence`, `.event-publishing`, `.proxy`, `.main`, `.shared-library`.
- **Common plugin class.** `ArchitectureRulesPlugin` in `buildSrc/src/main/groovy`, applied by the root `subprojects` block.
- **Extension.** `architectureRules { messageCopyPackages = [...] }` on each project.
- **Tasks.** `checkProjectDependencies` and `checkArchitectureRules` per project with a role, `checkSubdomainGraph` on the root. `check` in each project depends on the first two; the root `check` depends on the third. `compileAll` is unchanged.
- **System properties read by the ArchUnit rules.** `architectureRules.role`, `architectureRules.group`, `architectureRules.subdomain`, `architectureRules.package`, `architectureRules.classesDir`.
- **Subproject.** `architecture-rules`, a `java-library` with ArchUnit and JUnit Jupiter as `implementation` dependencies, included from `settings.gradle`.
- **`buildSrc/build.gradle`.** Applies `java-gradle-plugin`, declares the role plugin ids, and adds the Eventuate snapshot repository and the test plugin artifacts at `eventuateTestPluginsVersion` from `gradle.properties`.

## Non-functional requirements

- `checkProjectDependencies` and `checkSubdomainGraph` complete without compiling anything, so a build file violation is reported in seconds.
- `checkArchitectureRules` runs after `classes` and before the project's tests, and produces a standard JUnit XML report under `build/test-results/checkArchitectureRules`.
- The plugins and rules name no project of this repository. Every rule is a function of role, group and subdomain.
- Every plugin rule and every ArchUnit rule has a test named after its rule identifier: `buildSrc/src/test` with Gradle's `ProjectBuilder` for the plugin, fixture classes in `architecture-rules/src/test` for ArchUnit.

## Scenarios and workflows

### Primary end-to-end scenario: the repository builds under the rules

Every build file in this repository applies its role plugin, the HEX-7 violation recorded in the rules document is fixed, and `./gradlew build` runs `checkProjectDependencies`, `checkSubdomainGraph` and `checkArchitectureRules` in every project with a role and passes.

### A domain project depends on an adapter

A developer adds `implementation project(':customer-management:customer-management-persistence')` to `customer-management/customer-management-domain/build.gradle`. `./gradlew build` fails in `checkProjectDependencies` before compilation, naming HEX-1 (the domain does not depend on adapters), the dependency and its configuration.

### A test source set depends on another subdomain's domain

A second subdomain `b` exists. A developer adds `testImplementation project(':b:b-domain')` to a `customer-management` project. The build fails naming SUB-2 (Gradle dependencies between subdomains are on API projects only).

### A domain project pulls in infrastructure

A developer adds `implementation "org.springframework.boot:spring-boot-starter-webmvc"` to the domain project. The build fails naming HEX-4 (domain projects stay free of infrastructure technology), the dependency and the permitted categories. A developer instead writes a domain class with a field of type `org.springframework.web.client.RestClient`, which a permitted starter brings transitively. The build fails in `checkArchitectureRules` naming HEX-4, the class and the forbidden class.

### An API project gains a configuration class

A developer adds a class annotated `@Configuration` to an `api` project. The build fails in `checkArchitectureRules` naming HEX-6 (each project wires itself) and the class.

### A project is added without a role

A developer creates `customer-management/customer-management-payment-proxy` with a build file that applies no role plugin. The build fails after evaluation, stating that the project has no role and listing the role plugin ids.

### A project is misnamed

A developer creates `customer-management/customer-proxy` applying `io.eventuate.architecture.roles.proxy`. The build fails stating that the name does not match the pattern `<subdomain>-<other>-proxy`.

### A generated service is checked

The skeleton generator copies the template for a new service with two subdomains. The copied build files carry their role plugin lines, and the first `./gradlew build` of the generated service runs all three check tasks in every project.

## Constraints and assumptions

- The implementation lives in this repository: plugins in `buildSrc`, rules in the `architecture-rules` subproject. Publishing to the Eventuate plugin repository is a later idea.
- `buildSrc` cannot depend on a subproject, so the common plugin runs the ArchUnit rules through a `Test` task rather than calling them.
- The HEX-7 (main contains no business logic) violation in `customer-service-main/build.gradle` is fixed as the rules document's TODO section states: the three `implementation` project dependencies become `runtimeOnly` plus `componentTestImplementation`.
- HEX-4 (domain projects stay free of infrastructure technology) and HEX-9 (the message broker is declared in main only) already hold in this repository, since the sagas, command-api, persistence and event-subscribers build files were changed on 2026-10-22. The plugin's HEX-4 allow-lists include the Eventuate Flyway migration support for `sagas`.
- No known-violations list is kept, neither `FreezingArchRule` nor an equivalent for the plugin checks. The checks pass on their first run because the HEX-7 violation is fixed first.
- SUB-5 (each subdomain owns its data) and the "how a mechanism is used" part of SUB-3 (four interaction mechanisms) are not enforced, as the enforcement document's "Not enforced" section states.
- `other-service` is outside the rules and the plugins are not applied to it.
- Gradle 8.14 and the Java 17 toolchain, as the root build file sets them.

## Acceptance criteria

1. `./gradlew build` passes on this repository with every build file applying its role plugin and no project exempt other than `architecture-rules`.
2. Each scenario above that describes a violation produces a build failure naming the stated rule identifier, demonstrated by a test in `buildSrc/src/test` or `architecture-rules/src/test` named after the rule.
3. Removing the role plugin line from any build file fails the build after evaluation.
4. `./gradlew checkProjectDependencies checkSubdomainGraph` runs no `JavaCompile` task.
5. `grep -r customer-management buildSrc/src/main architecture-rules/src/main` finds nothing.
6. The rules document's TODO section is empty and its current-state section records no violation.

## Change History

### 2026-10-22: HEX-4 and HEX-9 resolved in the build files

The sagas project keeps its Eventuate Flyway dependencies under a new HEX-4 category, and the broker artifacts now live in main. Only the HEX-7 violation remains to fix.

### 2026-10-22: Add HEX-9

The rules document gained HEX-9 (the message broker is declared in main only), which the plugin enforces. Added it to the plugin rule list and the constraints.

### 2026-10-22: Initial specification

Written from the enforcement design and the three decisions recorded in the discussion file.
