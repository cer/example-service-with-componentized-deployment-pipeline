package io.eventuate.architecture.rules.gradle

import groovy.transform.TupleConstructor
import org.gradle.api.Project

// A project with a role, its declared dependencies, its main Java sources and the roles of the projects in the build
@TupleConstructor(includeFields = true)
class CheckedProject {

    final String path
    final Role role
    final DeclaredDependencies dependencies
    final MainJavaSources mainJavaSources
    private final Map<String, Role> rolesByProjectPath

    static CheckedProject of(Project project) {
        new CheckedProject(project.path, roleOf(project), DeclaredDependencies.of(project),
                MainJavaSources.of(project), rolesByProjectPath(project.rootProject))
    }

    Role roleOfProject(String projectPath) {
        rolesByProjectPath[projectPath]
    }

    // The path of the subdomain directory containing the project, or null when its role is outside subdomains
    String subdomainOfProject(String projectPath) {
        roleOfProject(projectPath)?.inSubdomain ? parentPathOf(projectPath) : null
    }

    String getSubdomain() {
        subdomainOfProject(path)
    }

    private static String parentPathOf(String projectPath) {
        projectPath.substring(0, projectPath.lastIndexOf(':'))
    }

    private static Map<String, Role> rolesByProjectPath(Project rootProject) {
        rootProject.allprojects
                .findAll { roleOf(it) != null }
                .collectEntries { [(it.path): roleOf(it)] }
    }

    private static Role roleOf(Project project) {
        project.extensions.findByType(ArchitectureRulesExtension)?.role?.getOrNull()
    }
}
