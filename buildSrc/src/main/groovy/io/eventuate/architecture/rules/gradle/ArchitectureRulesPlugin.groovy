package io.eventuate.architecture.rules.gradle

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
            }
        }
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
