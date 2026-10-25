package io.eventuate.architecture.rules.gradle.rules

import io.eventuate.architecture.rules.gradle.project.CheckedProject
import io.eventuate.architecture.rules.gradle.project.DeclaredDependencies.DeclaredProjectDependency
import io.eventuate.architecture.rules.gradle.roles.Role
import io.eventuate.architecture.rules.gradle.roles.RoleGroup

class Sub7MainIsOnlyCompositionRoot implements ProjectDependencyRule {

    @Override
    List<RuleViolation> check(CheckedProject project) {
        if (project.role == Role.MAIN) {
            return []
        }
        adapterDependenciesOfMoreThanOneSubdomain(project) + dependenciesOnMain(project)
    }

    private static List<RuleViolation> adapterDependenciesOfMoreThanOneSubdomain(CheckedProject project) {
        List<DeclaredProjectDependency> adapterDependencies = project.dependencies.projectDependencies
                .findAll { project.roleOfProject(it.path)?.group == RoleGroup.ADAPTER }
        if (adapterDependencies.collect { project.subdomainOfProject(it.path) }.unique().size() < 2) {
            return []
        }
        adapterDependencies.collect {
            new RuleViolation(Rule.SUB_7,
                    "${project.path} declares ${it.configuration} project('${it.path}'), but only main depends on adapter projects of more than one subdomain")
        }
    }

    private static List<RuleViolation> dependenciesOnMain(CheckedProject project) {
        project.dependencies.projectDependencies
                .findAll { project.roleOfProject(it.path) == Role.MAIN }
                .collect {
                    new RuleViolation(Rule.SUB_7,
                            "${project.path} declares ${it.configuration} project('${it.path}'), which is main")
                }
    }
}
