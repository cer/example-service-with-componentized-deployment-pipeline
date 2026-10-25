package io.eventuate.architecture.rules.gradle.project

import groovy.transform.Immutable
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency

// The dependencies a project's build file declares, read without resolving any configuration
class DeclaredDependencies {

    @Immutable
    static class DeclaredProjectDependency {
        String configuration
        String path
    }

    @Immutable
    static class DeclaredExternalDependency {

        private static final Set<String> MAIN_SOURCE_SET_CONFIGURATIONS =
                ['api', 'implementation', 'compileOnly', 'runtimeOnly'] as Set

        String configuration
        String group
        String name

        boolean isInMainSourceSet() {
            configuration in MAIN_SOURCE_SET_CONFIGURATIONS
        }
    }

    final List<DeclaredProjectDependency> projectDependencies
    final List<DeclaredExternalDependency> externalDependencies

    DeclaredDependencies(List<DeclaredProjectDependency> projectDependencies,
                         List<DeclaredExternalDependency> externalDependencies) {
        this.projectDependencies = projectDependencies
        this.externalDependencies = externalDependencies
    }

    static DeclaredDependencies of(Project project) {
        List<Configuration> configurations = project.configurations.toList().findAll { !RulesProjectConfigurations.isRulesConfiguration(it) }
        new DeclaredDependencies(
                configurations.collectMany { declaredProjectDependencies(it) },
                configurations.collectMany { declaredExternalDependencies(it) })
    }

    private static List<DeclaredProjectDependency> declaredProjectDependencies(Configuration configuration) {
        configuration.dependencies.withType(ProjectDependency).collect {
            new DeclaredProjectDependency(configuration.name, it.path)
        }
    }

    private static List<DeclaredExternalDependency> declaredExternalDependencies(Configuration configuration) {
        configuration.dependencies.withType(ExternalModuleDependency).collect {
            new DeclaredExternalDependency(configuration.name, it.group, it.name)
        }
    }
}
