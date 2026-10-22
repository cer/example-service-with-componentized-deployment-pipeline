package io.eventuate.architecture.rules.gradle

// Test source sets do not ship, and HEX-9 places broker artifacts in integration test configurations,
// so only the main source set configurations are checked
class Hex4DomainDependencyList implements ProjectDependencyRule {

    private static final Set<String> MAIN_SOURCE_SET_CONFIGURATIONS =
            ['api', 'implementation', 'compileOnly', 'runtimeOnly'] as Set

    @Override
    List<RuleViolation> check(CheckedProject project) {
        if (project.role.group != RoleGroup.DOMAIN) {
            return []
        }
        project.dependencies.externalDependencies
                .findAll { it.configuration in MAIN_SOURCE_SET_CONFIGURATIONS }
                .findAll { !PermittedDomainDependencies.isPermitted(project.role, it.group, it.name) }
                .collect {
                    new RuleViolation(Rule.HEX_4,
                            "${project.path} declares ${it.configuration} \"${it.group}:${it.name}\", which is outside the permitted categories for ${project.role.roleName}: ${permittedCategoryNames(project.role)}")
                }
    }

    private static String permittedCategoryNames(Role role) {
        PermittedDomainDependencies.categoriesFor(role)*.categoryName.join(', ')
    }
}
