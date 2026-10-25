package io.eventuate.architecture.rules.gradle.rules

import io.eventuate.architecture.rules.gradle.RuleFixtureBuild
import io.eventuate.architecture.rules.gradle.roles.Role
import org.gradle.api.Project
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class Hex2AdaptersDependOnlyOnDomainTest {

    private final RuleFixtureBuild build = new RuleFixtureBuild()

    @Test
    void implementationOnAnotherAdapterFails() {
        Project aRestApi = build.subdomainProject('a', Role.REST_API)
        Project aPersistence = build.subdomainProject('a', Role.PERSISTENCE)
        build.dependsOn(aRestApi, 'implementation', aPersistence)
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(aRestApi)

        assertTrue(failure.startsWith('HEX-2 (adapters depend only on the domain)'), failure)
        assertTrue(failure.contains(':a:a-persistence'), failure)
        assertTrue(failure.contains('implementation'), failure)
    }

    @Test
    void implementationOnDomainSagasAndSharedLibraryPasses() {
        Project aRestApi = build.subdomainProject('a', Role.REST_API)
        build.dependsOn(aRestApi, 'implementation', build.subdomainProject('a', Role.DOMAIN))
        build.dependsOn(aRestApi, 'implementation', build.subdomainProject('a', Role.SAGAS))
        build.dependsOn(aRestApi, 'implementation', build.projectWithRole(build.rootProject, 'common', Role.SHARED_LIBRARY))
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(aRestApi))
    }

    @Test
    void testImplementationOnItselfPasses() {
        Project aRestApi = build.subdomainProject('a', Role.REST_API)
        build.dependsOn(aRestApi, 'testImplementation', aRestApi)
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(aRestApi))
    }

    @Test
    void testImplementationOnMainFails() {
        Project aRestApi = build.subdomainProject('a', Role.REST_API)
        Project main = build.projectWithRole(build.rootProject, 'x-main', Role.MAIN)
        build.dependsOn(aRestApi, 'testImplementation', main)
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(aRestApi)

        assertTrue(failure.startsWith('HEX-2 (adapters depend only on the domain)'), failure)
        assertTrue(failure.contains(':x-main'), failure)
        assertTrue(failure.contains('testImplementation'), failure)
    }
}
