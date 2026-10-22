package io.eventuate.architecture.rules.gradle

// The rule identifiers and titles in docs/design/architecture-rules.md
enum Rule {
    HEX_1('HEX-1', 'the domain does not depend on adapters'),
    HEX_2('HEX-2', 'adapters depend only on the domain'),
    HEX_4('HEX-4', 'domain projects stay free of infrastructure technology'),
    HEX_5('HEX-5', 'one project, one package'),
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
