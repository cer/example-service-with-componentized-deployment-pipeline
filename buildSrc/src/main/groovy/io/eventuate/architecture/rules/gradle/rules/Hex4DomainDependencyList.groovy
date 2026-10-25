package io.eventuate.architecture.rules.gradle.rules

import io.eventuate.architecture.rules.gradle.project.CheckedProject
import io.eventuate.architecture.rules.gradle.roles.Role
import io.eventuate.architecture.rules.gradle.roles.RoleGroup

// Test source sets do not ship, and HEX-9 places broker artifacts in integration test configurations,
// so only the main source set configurations are checked
class Hex4DomainDependencyList implements ProjectDependencyRule {

    @Override
    List<RuleViolation> check(CheckedProject project) {
        if (project.role.group != RoleGroup.DOMAIN) {
            return []
        }
        project.dependencies.externalDependencies
                .findAll { it.inMainSourceSet }
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
