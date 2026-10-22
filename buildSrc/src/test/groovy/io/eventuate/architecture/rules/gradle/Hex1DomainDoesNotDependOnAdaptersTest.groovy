package io.eventuate.architecture.rules.gradle

import org.gradle.api.Project
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class Hex1DomainDoesNotDependOnAdaptersTest {

    private final RuleFixtureBuild build = new RuleFixtureBuild()

    @ParameterizedTest
    @ValueSource(strings = ['implementation', 'testImplementation'])
    void domainDependingOnPersistenceFails(String configuration) {
        Project domain = build.subdomainProject('a', Role.DOMAIN)
        Project persistence = build.subdomainProject('a', Role.PERSISTENCE)
        build.dependsOn(domain, configuration, persistence)
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(domain)

        assertTrue(failure.startsWith('HEX-1 (the domain does not depend on adapters)'), failure)
        assertTrue(failure.contains(':a:a-persistence'), failure)
        assertTrue(failure.contains(configuration), failure)
    }

    @Test
    void domainWithNoAdapterDependencyPasses() {
        Project domain = build.subdomainProject('a', Role.DOMAIN)
        build.subdomainProject('a', Role.PERSISTENCE)
        build.dependsOn(domain, 'implementation', 'org.springframework.boot:spring-boot-starter-data-jpa')
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(domain))
    }

    @Test
    void checkDependsOnCheckProjectDependencies() {
        Project domain = build.subdomainProject('a', Role.DOMAIN)
        build.evaluate()

        Set<String> checkDependencies = domain.tasks.getByName('check').taskDependencies
                .getDependencies(null).collect { it.name } as Set

        assertTrue(checkDependencies.contains('checkProjectDependencies'), checkDependencies.toString())
    }
}
