package io.eventuate.architecture.rules.gradle.rules

import io.eventuate.architecture.rules.gradle.RuleFixtureBuild
import io.eventuate.architecture.rules.gradle.roles.Role
import org.gradle.api.Project
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class Sub3FourInteractionMechanismsTest {

    private static final String SUB_3 = 'SUB-3 (four interaction mechanisms)'

    private final RuleFixtureBuild build = new RuleFixtureBuild()

    @Test
    void eventSubscribersOnAnotherSubdomainsApiFails() {
        Project aEventSubscribers = build.subdomainProject('a', Role.EVENT_SUBSCRIBERS)
        build.dependsOn(aEventSubscribers, 'implementation', build.subdomainProject('b', Role.API))
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(aEventSubscribers)

        assertViolationOfSub3Naming(failure, ':b:b-api')
    }

    @Test
    void sagasOnAnotherSubdomainsApiObserverFails() {
        Project aSagas = build.subdomainProject('a', Role.SAGAS)
        build.dependsOn(aSagas, 'implementation', apiObserver('b'))
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(aSagas)

        assertViolationOfSub3Naming(failure, ':b:b-api-observer')
    }

    @Test
    void domainOnAnotherSubdomainsApiObserverPasses() {
        Project aDomain = build.subdomainProject('a', Role.DOMAIN)
        build.dependsOn(aDomain, 'implementation', apiObserver('b'))
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(aDomain))
    }

    @Test
    void sagasOnAnotherSubdomainsApiPasses() {
        Project aSagas = build.subdomainProject('a', Role.SAGAS)
        build.dependsOn(aSagas, 'implementation', build.subdomainProject('b', Role.API))
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(aSagas))
    }

    private Project apiObserver(String subdomain) {
        build.projectWithRole(build.subdomainDirectory(subdomain), "${subdomain}-api-observer", Role.API_FACET)
    }

    private static void assertViolationOfSub3Naming(String failure, String dependencyPath) {
        String sub3Line = failure?.readLines()?.find { it.startsWith(SUB_3) }
        assertTrue(sub3Line != null, failure)
        assertTrue(sub3Line.contains(dependencyPath), failure)
    }
}
