package io.eventuate.architecture.rules.gradle

// SUB-1 (subdomains interact only through APIs) follows from this rule, as
// docs/design/architecture-rules-enforcement.md says: a class can depend on another
// subdomain's classes only through a project dependency, which this rule limits to API projects
class Sub2DependenciesBetweenSubdomainsAreOnApiProjects implements ProjectDependencyRule {

    private static final Set<Role> API_ROLES = [Role.API, Role.API_FACET] as Set

    @Override
    List<RuleViolation> check(CheckedProject project) {
        if (project.subdomain == null) {
            return []
        }
        project.dependencies.projectDependencies
                .findAll { isNonApiProjectOfAnotherSubdomain(project, it.path) }
                .collect {
                    new RuleViolation(Rule.SUB_2,
                            "${project.path} declares ${it.configuration} project('${it.path}'), which is a non-API project of another subdomain")
                }
    }

    private static boolean isNonApiProjectOfAnotherSubdomain(CheckedProject project, String dependencyPath) {
        isInAnotherSubdomain(project, dependencyPath) && !(project.roleOfProject(dependencyPath) in API_ROLES)
    }

    private static boolean isInAnotherSubdomain(CheckedProject project, String dependencyPath) {
        String dependencySubdomain = project.subdomainOfProject(dependencyPath)
        dependencySubdomain != null && dependencySubdomain != project.subdomain
    }
}
