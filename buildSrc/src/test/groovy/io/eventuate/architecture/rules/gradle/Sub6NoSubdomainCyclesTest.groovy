package io.eventuate.architecture.rules.gradle

import org.gradle.api.Task
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class Sub6NoSubdomainCyclesTest {

    private static final String SUB_6 = 'SUB-6 (no cycles between subdomains via shared code)'

    private final RuleFixtureBuild build = new RuleFixtureBuild()

    @Test
    void cycleOfApiDependenciesBetweenSubdomainsFails() {
        build.dependsOn(build.subdomainProject('a', Role.DOMAIN), 'implementation', build.subdomainProject('b', Role.API))
        build.dependsOn(build.subdomainProject('b', Role.DOMAIN), 'implementation', build.subdomainProject('a', Role.API))
        build.evaluate()

        String failure = build.checkSubdomainGraphFailure()

        assertTrue(failure?.startsWith(SUB_6), failure)
        assertTrue(failure.contains('a -> b -> a'), failure)
    }

    @Test
    void acyclicApiDependencyBetweenSubdomainsPasses() {
        build.dependsOn(build.subdomainProject('a', Role.DOMAIN), 'implementation', build.subdomainProject('b', Role.API))
        build.subdomainProject('b', Role.DOMAIN)
        build.evaluate()

        assertNull(build.checkSubdomainGraphFailure())
    }

    @Test
    void rootCheckDependsOnCheckSubdomainGraph() {
        Task check = build.rootProject.tasks.getByName('check')

        Set<String> checkDependencies = check.taskDependencies.getDependencies(check)*.name as Set

        assertTrue(CheckSubdomainGraphTask.NAME in checkDependencies, checkDependencies.toString())
    }
}
