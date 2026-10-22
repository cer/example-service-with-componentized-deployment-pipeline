package io.eventuate.architecture.rules.gradle

// The rule identifiers and titles in docs/design/architecture-rules.md
enum Rule {
    HEX_1('HEX-1', 'the domain does not depend on adapters'),
    HEX_2('HEX-2', 'adapters depend only on the domain'),
    HEX_4('HEX-4', 'domain projects stay free of infrastructure technology'),
    HEX_5('HEX-5', 'one project, one package'),
    HEX_7('HEX-7', 'main contains no business logic'),
    HEX_8('HEX-8', 'API projects contain only interfaces and value types'),
    HEX_9('HEX-9', 'the message broker is declared in main only'),
    SUB_2('SUB-2', 'Gradle dependencies between subdomains are on API projects only'),
    SUB_7('SUB-7', 'main is the only composition root'),
    SUB_8('SUB-8', 'shared libraries hold only value types')

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
