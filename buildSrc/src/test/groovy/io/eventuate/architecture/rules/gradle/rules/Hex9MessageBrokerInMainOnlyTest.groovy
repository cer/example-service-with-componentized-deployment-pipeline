package io.eventuate.architecture.rules.gradle.rules

import io.eventuate.architecture.rules.gradle.RuleFixtureBuild
import io.eventuate.architecture.rules.gradle.plugins.RolePlugin
import io.eventuate.architecture.rules.gradle.roles.Role
import org.gradle.api.Project
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class Hex9MessageBrokerInMainOnlyTest {

    private static final String JDBC_KAFKA = 'io.eventuate.tram.core:eventuate-tram-spring-jdbc-kafka'
    private static final String CONSUMER_KAFKA = 'io.eventuate.tram.core:eventuate-tram-spring-consumer-kafka'

    private final RuleFixtureBuild build = new RuleFixtureBuild()

    @Test
    void implementationInSagasFails() {
        Project sagas = build.subdomainProject('a', Role.SAGAS)
        build.dependsOn(sagas, 'implementation', JDBC_KAFKA)
        build.evaluate()

        // HEX-4 (domain projects stay free of infrastructure technology) also reports the artifact in sagas
        String failure = hex9Violation(build.checkProjectDependenciesFailure(sagas))

        assertTrue(failure.contains(JDBC_KAFKA), failure)
        assertTrue(failure.contains('implementation'), failure)
    }

    @Test
    void testImplementationInAdapterFails() {
        Project eventSubscribers = build.subdomainProject('a', Role.EVENT_SUBSCRIBERS)
        build.dependsOn(eventSubscribers, 'testImplementation', CONSUMER_KAFKA)
        build.evaluate()

        String failure = hex9Violation(build.checkProjectDependenciesFailure(eventSubscribers))

        assertTrue(failure.contains(CONSUMER_KAFKA), failure)
        assertTrue(failure.contains('testImplementation'), failure)
    }

    @Test
    void integrationTestAndComponentTestConfigurationsPassInSagas() {
        Project sagas = build.subdomainProject('a', Role.SAGAS)
        sagas.pluginManager.apply(RolePlugin.COMPONENT_TESTS_PLUGIN_ID)
        build.dependsOn(sagas, 'integrationTestImplementation', JDBC_KAFKA)
        build.dependsOn(sagas, 'integrationTestRuntimeOnly', CONSUMER_KAFKA)
        build.dependsOn(sagas, 'componentTestImplementation', JDBC_KAFKA)
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(sagas))
    }

    @Test
    void implementationInMainPasses() {
        Project main = build.projectWithRole(build.rootProject, 'x-main', Role.MAIN)
        build.dependsOn(main, 'implementation', JDBC_KAFKA)
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(main))
    }

    private static String hex9Violation(String failure) {
        String violation = failure?.readLines()?.find { it.startsWith('HEX-9 (the message broker is declared in main only)') }
        assertNotNull(violation, failure)
        violation
    }
}
