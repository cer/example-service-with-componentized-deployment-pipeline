package io.eventuate.architecture.rules.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class ArchitectureRulesPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = 'architectureRules'

    @Override
    void apply(Project project) {
        ArchitectureRulesExtension extension = project.extensions.create(EXTENSION_NAME, ArchitectureRulesExtension)
        project.afterEvaluate {
            if (extension.role.present) {
                registerCheckProjectDependencies(project)
            } else if (!isDirectoryOfProjects(project)) {
                throw new GradleException(noRoleMessage(project))
            }
        }
    }

    // A subdomain directory such as customer-management is a project only because settings.gradle includes its children
    private static boolean isDirectoryOfProjects(Project project) {
        !project.childProjects.isEmpty()
    }

    private static String noRoleMessage(Project project) {
        "Project ${project.path} has no architecture role. Apply one of these role plugins in its build file: " +
                Role.values().collect { it.pluginId }.join(', ')
    }

    private static void registerCheckProjectDependencies(Project project) {
        def checkProjectDependencies = project.tasks.register(CheckProjectDependenciesTask.NAME, CheckProjectDependenciesTask) {
            it.group = 'verification'
            it.description = 'Checks the project dependencies declared by this project against the architecture rules.'
            it.checkedProject.set(project.provider { CheckedProject.of(project) })
        }
        project.pluginManager.withPlugin('lifecycle-base') {
            project.tasks.named('check') { it.dependsOn(checkProjectDependencies) }
        }
    }
}
