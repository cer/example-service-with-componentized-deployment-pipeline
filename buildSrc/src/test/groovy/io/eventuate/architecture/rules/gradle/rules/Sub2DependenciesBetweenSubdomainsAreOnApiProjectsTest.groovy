package io.eventuate.architecture.rules.gradle.rules

import io.eventuate.architecture.rules.gradle.RuleFixtureBuild
import io.eventuate.architecture.rules.gradle.roles.Role
import org.gradle.api.Project
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class Sub2DependenciesBetweenSubdomainsAreOnApiProjectsTest {

    private final RuleFixtureBuild build = new RuleFixtureBuild()

    @Test
    void testImplementationOnAnotherSubdomainsDomainFails() {
        Project aDomain = build.subdomainProject('a', Role.DOMAIN)
        Project bDomain = build.subdomainProject('b', Role.DOMAIN)
        build.dependsOn(aDomain, 'testImplementation', bDomain)
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(aDomain)

        assertTrue(failure.startsWith('SUB-2 (Gradle dependencies between subdomains are on API projects only)'), failure)
        assertTrue(failure.contains(':b:b-domain'), failure)
        assertTrue(failure.contains('testImplementation'), failure)
    }

    @Test
    void implementationOnAnotherSubdomainsCommandApiFails() {
        Project aDomain = build.subdomainProject('a', Role.DOMAIN)
        Project bCommandApi = build.subdomainProject('b', Role.COMMAND_API)
        build.dependsOn(aDomain, 'implementation', bCommandApi)
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(aDomain)

        assertTrue(failure.contains('SUB-2 (Gradle dependencies between subdomains are on API projects only)'), failure)
        assertTrue(failure.contains(':b:b-command-api'), failure)
        assertTrue(failure.contains('implementation'), failure)
    }

    @Test
    void implementationOnAnotherSubdomainsApiAndApiFacetPasses() {
        Project aDomain = build.subdomainProject('a', Role.DOMAIN)
        Project bApi = build.subdomainProject('b', Role.API)
        Project bApiObserver = build.projectWithRole(build.subdomainDirectory('b'), 'b-api-observer', Role.API_FACET)
        build.dependsOn(aDomain, 'implementation', bApi)
        build.dependsOn(aDomain, 'implementation', bApiObserver)
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(aDomain))
    }
}
