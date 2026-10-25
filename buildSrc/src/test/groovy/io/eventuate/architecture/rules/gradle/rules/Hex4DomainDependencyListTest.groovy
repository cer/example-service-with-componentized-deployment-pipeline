package io.eventuate.architecture.rules.gradle.rules

import io.eventuate.architecture.rules.gradle.RuleFixtureBuild
import io.eventuate.architecture.rules.gradle.roles.Role
import org.gradle.api.Project
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class Hex4DomainDependencyListTest {

    private static final String SAGAS_FLYWAY = 'io.eventuate.tram.sagas:eventuate-tram-sagas-spring-flyway'

    private final RuleFixtureBuild build = new RuleFixtureBuild()

    @Test
    void webMvcStarterInDomainFails() {
        Project domain = build.subdomainProject('a', Role.DOMAIN)
        build.dependsOn(domain, 'implementation', 'org.springframework.boot:spring-boot-starter-webmvc')
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(domain)

        assertTrue(failure.startsWith('HEX-4 (domain projects stay free of infrastructure technology)'), failure)
        assertTrue(failure.contains('org.springframework.boot:spring-boot-starter-webmvc'), failure)
        assertTrue(failure.contains('Spring context'), failure)
        assertTrue(failure.contains('JPA and Spring Data'), failure)
        assertTrue(failure.contains('Eventuate events API'), failure)
        assertTrue(failure.contains('Money value type'), failure)
        assertFalse(failure.contains('Eventuate Sagas orchestration DSL'), failure)
    }

    @Test
    void sagasFlywayPassesInSagas() {
        Project sagas = build.subdomainProject('a', Role.SAGAS)
        build.dependsOn(sagas, 'runtimeOnly', SAGAS_FLYWAY)
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(sagas))
    }

    @Test
    void sagasFlywayFailsInDomain() {
        Project domain = build.subdomainProject('a', Role.DOMAIN)
        build.dependsOn(domain, 'runtimeOnly', SAGAS_FLYWAY)
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(domain)

        assertTrue(failure.startsWith('HEX-4 (domain projects stay free of infrastructure technology)'), failure)
        assertTrue(failure.contains(SAGAS_FLYWAY), failure)
        assertTrue(failure.contains('runtimeOnly'), failure)
    }

    @Test
    void permittedCategoriesPassInDomain() {
        Project domain = build.subdomainProject('a', Role.DOMAIN)
        build.dependsOn(domain, 'implementation', 'org.springframework.boot:spring-boot-starter-data-jpa')
        build.dependsOn(domain, 'api', 'io.eventuate.tram.core:eventuate-tram-spring-events')
        build.dependsOn(domain, 'api', 'io.eventuate.examples.common:eventuate-examples-common-money-jakarta9')
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(domain))
    }

    @Test
    void testDependencyPassesInDomain() {
        Project domain = build.subdomainProject('a', Role.DOMAIN)
        build.dependsOn(domain, 'testImplementation', 'org.springframework.boot:spring-boot-starter-test')
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(domain))
    }
}
