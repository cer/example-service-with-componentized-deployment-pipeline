package io.eventuate.architecture.rules.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class CheckArchitectureRulesTaskTest {

    private final RuleFixtureBuild build = new RuleFixtureBuild()

    @ParameterizedTest
    @EnumSource(Role)
    void projectWithRoleHasCheckArchitectureRulesTask(Role role) {
        Project project = projectWithRole(role)
        build.evaluate()

        assertTrue(checkArchitectureRules(project) instanceof Test)
    }

    @ParameterizedTest
    @EnumSource(Role)
    void systemPropertiesDescribeTheProject(Role role) {
        Project project = projectWithRole(role)
        build.evaluate()

        Map<String, Object> systemProperties = checkArchitectureRules(project).systemProperties
        ArchitectureRulesExtension extension = project.extensions.getByType(ArchitectureRulesExtension)
        assertEquals(role.roleName, systemProperties['architectureRules.role'])
        assertEquals(RuleFixtureBuild.ROOT_PACKAGE, systemProperties['architectureRules.group'])
        assertEquals(extension.subdomain.getOrElse(''), systemProperties['architectureRules.subdomain'])
        assertEquals(extension.projectPackage.get(), systemProperties['architectureRules.package'])
        assertEquals(mainClassesDirectory(project), systemProperties['architectureRules.classesDir'])
    }

    @ParameterizedTest
    @EnumSource(Role)
    void checkDependsOnCheckArchitectureRules(Role role) {
        Project project = projectWithRole(role)
        build.evaluate()

        Task check = project.tasks.getByName('check')
        assertTrue(check.taskDependencies.getDependencies(check).contains(checkArchitectureRules(project)))
    }

    @ParameterizedTest
    @EnumSource(Role)
    void testMustRunAfterCheckArchitectureRules(Role role) {
        Project project = projectWithRole(role)
        build.evaluate()

        Task test = project.tasks.getByName('test')
        assertTrue(test.mustRunAfter.getDependencies(test).contains(checkArchitectureRules(project)))
    }

    @org.junit.jupiter.api.Test
    void exemptProjectHasNoCheckArchitectureRulesTask() {
        build.rootProject.extensions.getByType(ArchitectureRulesExtension).exemptProjects.set([':architecture-rules'])
        Project project = build.architectureRulesProject()
        build.evaluationFailure(project)

        assertNull(project.tasks.findByName(CheckArchitectureRules.NAME))
    }

    @ParameterizedTest
    @EnumSource(Role)
    void checkProjectDependenciesIgnoresTheRulesProjectConfigurations(Role role) {
        build.rootProject.extensions.getByType(ArchitectureRulesExtension).rulesProject.set(':architecture-rules')
        build.architectureRulesProject()
        Project project = projectWithRole(role)
        build.evaluate()
        addRulesProjectDependenciesAsResolutionDoes(project)

        assertNull(build.checkProjectDependenciesFailure(project))
    }

    private Project projectWithRole(Role role) {
        switch (role) {
            case Role.MAIN: return build.projectWithRole(build.rootProject, 'x-main', role)
            case Role.SHARED_LIBRARY: return build.projectWithRole(build.rootProject, 'common', role)
            default: return build.subdomainProject('a', role)
        }
    }

    // checkArchitectureRules resolves these configurations, which runs their withDependencies actions
    private static void addRulesProjectDependenciesAsResolutionDoes(Project project) {
        ['architectureRulesClasses', 'architectureRulesRuntime'].each {
            project.configurations.getByName(it).incoming.dependencies
        }
    }

    private static Task checkArchitectureRules(Project project) {
        project.tasks.getByName(CheckArchitectureRules.NAME)
    }

    private static String mainClassesDirectory(Project project) {
        project.extensions.getByType(SourceSetContainer).getByName('main').java.destinationDirectory.get().asFile.absolutePath
    }
}
