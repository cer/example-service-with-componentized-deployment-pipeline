package io.eventuate.architecture.rules.gradle.project

import groovy.transform.TupleConstructor
import io.eventuate.architecture.rules.gradle.roles.Role
import org.gradle.api.Project

// A project with a role, its declared dependencies, its Java sources and the roles and packages of the projects in the build
@TupleConstructor(includeFields = true)
class CheckedProject {

    final String path
    final Role role
    final DeclaredDependencies dependencies
    final MainJavaSources mainJavaSources
    final CompileConfigurationSources compileConfigurationSources
    private final Map<String, Role> rolesByProjectPath
    private final Map<String, DeclaredPackages> declaredPackagesByProjectPath

    static CheckedProject of(Project project) {
        List<Project> projectsWithRole = projectsWithRole(project.rootProject)
        new CheckedProject(project.path, roleOf(project), DeclaredDependencies.of(project),
                MainJavaSources.of(project), CompileConfigurationSources.of(project),
                projectsWithRole.collectEntries { [(it.path): roleOf(it)] },
                projectsWithRole.collectEntries { [(it.path): declaredPackagesOf(it)] })
    }

    Role roleOfProject(String projectPath) {
        rolesByProjectPath[projectPath]
    }

    DeclaredPackages declaredPackagesOfProject(String projectPath) {
        declaredPackagesByProjectPath[projectPath]
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

    private static List<Project> projectsWithRole(Project rootProject) {
        rootProject.allprojects.findAll { roleOf(it) != null }.toList()
    }

    private static Role roleOf(Project project) {
        project.extensions.findByType(ArchitectureRulesExtension)?.role?.getOrNull()
    }

    private static DeclaredPackages declaredPackagesOf(Project project) {
        DeclaredPackages.of(project.extensions.getByType(ArchitectureRulesExtension))
    }
}
