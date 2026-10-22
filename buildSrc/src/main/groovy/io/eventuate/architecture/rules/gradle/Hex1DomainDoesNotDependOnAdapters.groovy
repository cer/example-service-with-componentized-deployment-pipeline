package io.eventuate.architecture.rules.gradle

class Hex1DomainDoesNotDependOnAdapters implements ProjectDependencyRule {

    @Override
    List<RuleViolation> check(CheckedProject project) {
        if (project.role.group != RoleGroup.DOMAIN) {
            return []
        }
        project.dependencies.projectDependencies
                .findAll { project.roleOfProject(it.path)?.group == RoleGroup.ADAPTER }
                .collect {
                    new RuleViolation(Rule.HEX_1,
                            "${project.path} declares ${it.configuration} project('${it.path}'), which is an adapter")
                }
    }
}
