package io.eventuate.architecture.rules.gradle.rules

import io.eventuate.architecture.rules.gradle.roles.Role

// The permitted categories of HEX-4 in docs/design/architecture-rules.md, as allow-lists of group and artifact
enum PermittedDomainDependencies {
    SPRING_CONTEXT('Spring context', false,
            'org.springframework:spring-context',
            'org.springframework:spring-tx',
            'org.springframework.boot:spring-boot-starter'),
    JPA_AND_SPRING_DATA('JPA and Spring Data', false,
            'org.springframework.boot:spring-boot-starter-data-jpa',
            'org.springframework.data:spring-data-jpa',
            'org.springframework.data:spring-data-commons',
            'jakarta.persistence:jakarta.persistence-api'),
    EVENTUATE_EVENTS_API('Eventuate events API', false,
            'io.eventuate.tram.core:eventuate-tram-spring-events'),
    MONEY_VALUE_TYPE('Money value type', false,
            'io.eventuate.examples.common:eventuate-examples-common-money-jakarta9'),
    EVENTUATE_SAGAS_ORCHESTRATION_DSL('Eventuate Sagas orchestration DSL', true,
            'io.eventuate.tram.sagas:eventuate-tram-sagas-spring-orchestration-simple-dsl-starter'),
    EVENTUATE_FLYWAY_MIGRATION_SUPPORT('Eventuate Flyway migration support', true,
            'io.eventuate.tram.core:eventuate-tram-spring-flyway',
            'io.eventuate.tram.sagas:eventuate-tram-sagas-spring-flyway')

    final String categoryName
    private final boolean sagasOnly
    private final Set<String> artifacts

    PermittedDomainDependencies(String categoryName, boolean sagasOnly, String... artifacts) {
        this.categoryName = categoryName
        this.sagasOnly = sagasOnly
        this.artifacts = artifacts as Set
    }

    static List<PermittedDomainDependencies> categoriesFor(Role role) {
        values().findAll { !it.sagasOnly || role == Role.SAGAS }
    }

    static boolean isPermitted(Role role, String group, String name) {
        categoriesFor(role).any { "${group}:${name}".toString() in it.artifacts }
    }
}
