package io.eventuate.architecture.rules.gradle

// The dependency shape of SUB-3. How a mechanism is used is not enforced
class Sub3FourInteractionMechanisms implements ProjectDependencyRule {

    private static final Set<Role> OPERATION_CALLER_ROLES = [Role.DOMAIN, Role.SAGAS] as Set
    private static final Set<Role> OBSERVER_ROLES = [Role.DOMAIN] as Set
    private static final String OBSERVER_FACET = 'observer'

    @Override
    List<RuleViolation> check(CheckedProject project) {
        if (project.subdomain == null) {
            return []
        }
        project.dependencies.projectDependencies
                .findAll { isApiProjectOfAnotherSubdomain(project, it.path) }
                .findAll { !(project.role in permittedRoles(it.path)) }
                .collect {
                    new RuleViolation(Rule.SUB_3,
                            "${project.path} declares ${it.configuration} project('${it.path}'), but only ${permittedRoles(it.path)*.roleName.join(' and ')} projects depend on that API project of another subdomain")
                }
    }

    private static boolean isApiProjectOfAnotherSubdomain(CheckedProject project, String dependencyPath) {
        String dependencySubdomain = project.subdomainOfProject(dependencyPath)
        dependencySubdomain != null && dependencySubdomain != project.subdomain &&
                project.roleOfProject(dependencyPath)?.api
    }

    private static Set<Role> permittedRoles(String dependencyPath) {
        facetOf(dependencyPath) == OBSERVER_FACET ? OBSERVER_ROLES : OPERATION_CALLER_ROLES
    }

    // The facet of an api-facet project, or null for an api project, whose name does not match the api-facet pattern
    private static String facetOf(String dependencyPath) {
        List<String> segments = dependencyPath.split(':').toList()
        Role.API_FACET.placeholderValues(segments[-1], segments[-2])?.facet
    }
}
