# Architecture rules enforcement: discussion

The brainstorming for this idea took place while writing [docs/design/architecture-rules-enforcement.md](../../../design/architecture-rules-enforcement.md). That document records the alternatives in its "Discarded options" section and the chosen approach in "Approach". This file records the decisions taken after the design was written, and the classification.

## Questions and answers

**Should a project whose name does not match its declared role's pattern fail the build?**
Yes. The naming convention is a rule. A warning would let names drift from roles.

**Should the permitted dependency list of HEX-4 (domain projects stay free of infrastructure technology) be data in the plugin or configured per build?**
Data in the plugin. Every service built from the template has the same list, and adding a category is a plugin change, which matches the rule's last sentence.

**Should the role plugins carry build conventions from the first version?**
Yes. The role plugins for the adapter roles and `sagas` apply the integration-tests plugin, and `main` applies `ServicePlugin` and the component-tests plugin. The contract plugins stay in the build files, because whether a project owns or consumes contracts depends on the project, not its role: today `rest-api` and `event-publishing` are providers, `sagas` is a consumer and `command-api` has no contracts. This couples the rules plugin to the test plugins, which costs nothing because every service built from the template uses them. `ServicePlugin` already applies the Spring Boot plugin the same way.

**Should the design document move into this idea directory as `architecture-rules-enforcement-design.md`?**
No. The rules document and the enforcement document describe the template, not one idea, and outlive the idea. The idea files reference them.

## Classification

**Type:** C. Platform/infrastructure capability

**Rationale:** The capability is consumed by the service's Gradle build files, by developers adding projects, and by the skeleton generator that copies the template. End users of the service see nothing.
