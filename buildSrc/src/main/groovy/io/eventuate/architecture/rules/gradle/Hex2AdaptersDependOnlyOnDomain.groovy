package io.eventuate.architecture.rules.gradle

class Hex2AdaptersDependOnlyOnDomain implements ProjectDependencyRule {

    @Override
    List<RuleViolation> check(CheckedProject project) {
        if (project.role.group != RoleGroup.ADAPTER) {
            return []
        }
        project.dependencies.projectDependencies
                .findAll { !isPermittedDependency(project, it.path) }
                .collect {
                    new RuleViolation(Rule.HEX_2,
                            "${project.path} declares ${it.configuration} project('${it.path}'), which is neither a domain project of its subdomain nor a shared library")
                }
    }

    private static boolean isPermittedDependency(CheckedProject project, String dependencyPath) {
        isItself(project, dependencyPath) || isSharedLibrary(project, dependencyPath) ||
                isDomainProjectOfSameSubdomain(project, dependencyPath)
    }

    private static boolean isItself(CheckedProject project, String dependencyPath) {
        dependencyPath == project.path
    }

    private static boolean isSharedLibrary(CheckedProject project, String dependencyPath) {
        project.roleOfProject(dependencyPath)?.group == RoleGroup.SHARED_LIBRARY
    }

    private static boolean isDomainProjectOfSameSubdomain(CheckedProject project, String dependencyPath) {
        project.roleOfProject(dependencyPath)?.group == RoleGroup.DOMAIN &&
                project.subdomainOfProject(dependencyPath) == project.subdomain
    }
}
