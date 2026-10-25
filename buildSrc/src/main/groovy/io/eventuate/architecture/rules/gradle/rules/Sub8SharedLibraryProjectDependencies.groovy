package io.eventuate.architecture.rules.gradle.rules

import io.eventuate.architecture.rules.gradle.project.CheckedProject
import io.eventuate.architecture.rules.gradle.roles.Role
import io.eventuate.architecture.rules.gradle.roles.RoleGroup

// The project dependency clause of SUB-8. The contents clause is checked by ArchUnit
class Sub8SharedLibraryProjectDependencies implements ProjectDependencyRule {

    @Override
    List<RuleViolation> check(CheckedProject project) {
        if (project.role != Role.SHARED_LIBRARY) {
            return []
        }
        project.dependencies.projectDependencies
                .findAll { it.path != project.path && isSubdomainProjectOrSharedLibrary(project, it.path) }
                .collect {
                    new RuleViolation(Rule.SUB_8,
                            "${project.path} declares ${it.configuration} project('${it.path}'), but a shared library depends on no subdomain project or other shared library")
                }
    }

    private static boolean isSubdomainProjectOrSharedLibrary(CheckedProject project, String dependencyPath) {
        Role dependencyRole = project.roleOfProject(dependencyPath)
        dependencyRole?.inSubdomain || dependencyRole?.group == RoleGroup.SHARED_LIBRARY
    }
}
