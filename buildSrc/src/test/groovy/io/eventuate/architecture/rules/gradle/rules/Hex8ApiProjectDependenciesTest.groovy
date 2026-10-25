package io.eventuate.architecture.rules.gradle.rules

import io.eventuate.architecture.rules.gradle.RuleFixtureBuild
import io.eventuate.architecture.rules.gradle.roles.Role
import org.gradle.api.Project
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class Hex8ApiProjectDependenciesTest {

    private static final String HEX_8 = 'HEX-8 (API projects contain only interfaces and value types)'

    private final RuleFixtureBuild build = new RuleFixtureBuild()

    @Test
    void apiOnDomainProjectFails() {
        Project api = build.subdomainProject('a', Role.API)
        build.dependsOn(api, 'implementation', build.subdomainProject('a', Role.DOMAIN))
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(api)

        assertTrue(failure.startsWith(HEX_8), failure)
        assertTrue(failure.contains(':a:a-domain'), failure)
    }

    @Test
    void apiOnSpringContextFails() {
        Project api = build.subdomainProject('a', Role.API)
        build.dependsOn(api, 'implementation', 'org.springframework:spring-context')
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(api)

        assertTrue(failure.startsWith(HEX_8), failure)
        assertTrue(failure.contains('org.springframework:spring-context'), failure)
    }

    @Test
    void apiFacetOnApiOfSameSubdomainPasses() {
        Project apiObserver = build.projectWithRole(build.subdomainDirectory('a'), 'a-api-observer', Role.API_FACET)
        build.dependsOn(apiObserver, 'api', build.subdomainProject('a', Role.API))
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(apiObserver))
    }

    @Test
    void apiOnSharedLibraryPasses() {
        Project api = build.subdomainProject('a', Role.API)
        build.dependsOn(api, 'api', build.projectWithRole(build.rootProject, 'common', Role.SHARED_LIBRARY))
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(api))
    }

    @Test
    void apiOnMoneyValueTypePasses() {
        Project api = build.subdomainProject('a', Role.API)
        build.dependsOn(api, 'api', 'io.eventuate.examples.common:eventuate-examples-common-money-jakarta9')
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(api))
    }
}
