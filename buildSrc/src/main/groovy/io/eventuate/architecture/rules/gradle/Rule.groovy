package io.eventuate.architecture.rules.gradle

// The rule identifiers and titles in docs/design/architecture-rules.md
enum Rule {
    HEX_1('HEX-1', 'the domain does not depend on adapters'),
    SUB_2('SUB-2', 'Gradle dependencies between subdomains are on API projects only')

    final String identifier
    final String title

    Rule(String identifier, String title) {
        this.identifier = identifier
        this.title = title
    }

    @Override
    String toString() {
        "${identifier} (${title})"
    }
}
