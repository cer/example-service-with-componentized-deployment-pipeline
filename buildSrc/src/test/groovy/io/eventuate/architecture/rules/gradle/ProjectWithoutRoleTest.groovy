package io.eventuate.architecture.rules.gradle

import org.gradle.api.Project
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class ProjectWithoutRoleTest {

    private final RuleFixtureBuild build = new RuleFixtureBuild()

    @Test
    void projectWithoutRoleFailsEvaluation() {
        Project project = build.subdomainProjectWithoutRole('a', 'a-payment-proxy')

        String failure = build.evaluationFailure(project)

        assertTrue(failure.contains(':a:a-payment-proxy has no architecture role'), failure)
        Role.values().each { role ->
            assertTrue(failure.contains(role.pluginId), "${role.pluginId} missing from: ${failure}")
        }
    }

    @Test
    void projectWithRoleEvaluates() {
        Project project = build.subdomainProject('a', Role.PROXY)

        assertNull(build.evaluationFailure(project))
    }

    @Test
    void exemptProjectWithoutRoleEvaluates() {
        build.rootProject.extensions.getByType(ArchitectureRulesExtension).exemptProjects.set([':architecture-rules'])
        Project project = build.architectureRulesProject()

        assertNull(build.evaluationFailure(project))
    }

    @Test
    void subdomainDirectoryEvaluates() {
        build.subdomainProject('a', Role.DOMAIN)

        assertNull(build.evaluationFailure(build.subdomainDirectory('a')))
    }
}
