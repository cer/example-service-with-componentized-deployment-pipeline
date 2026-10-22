package io.eventuate.architecture.rules.gradle

// The project dependency clause of HEX-8. The contents clause is checked by ArchUnit
class Hex8ApiProjectDependencies implements ProjectDependencyRule {

    private static final List<String> FORBIDDEN_GROUP_PREFIXES = [
            'org.springframework',
            'jakarta.persistence',
            'org.hibernate',
            'io.eventuate.tram',
            'io.eventuate.common',
            'io.eventuate.messaging',
            'io.eventuate.platform',
    ]

    @Override
    List<RuleViolation> check(CheckedProject project) {
        if (!project.role.api) {
            return []
        }
        forbiddenProjectDependencyViolations(project) + forbiddenExternalDependencyViolations(project)
    }

    private static List<RuleViolation> forbiddenProjectDependencyViolations(CheckedProject project) {
        project.dependencies.projectDependencies
                .findAll { !isPermittedProjectDependency(project, it.path) }
                .collect {
                    new RuleViolation(Rule.HEX_8,
                            "${project.path} declares ${it.configuration} project('${it.path}'), which is neither an API project of its subdomain nor a shared library")
                }
    }

    private static List<RuleViolation> forbiddenExternalDependencyViolations(CheckedProject project) {
        project.dependencies.externalDependencies
                .findAll { it.inMainSourceSet }
                .findAll { isInfrastructureGroup(it.group) }
                .collect {
                    new RuleViolation(Rule.HEX_8,
                            "${project.path} declares ${it.configuration} \"${it.group}:${it.name}\", which is a Spring, JPA or Eventuate infrastructure artifact")
                }
    }

    private static boolean isPermittedProjectDependency(CheckedProject project, String dependencyPath) {
        dependencyPath == project.path || isSharedLibrary(project, dependencyPath) ||
                isApiProjectOfSameSubdomain(project, dependencyPath)
    }

    private static boolean isSharedLibrary(CheckedProject project, String dependencyPath) {
        project.roleOfProject(dependencyPath)?.group == RoleGroup.SHARED_LIBRARY
    }

    private static boolean isApiProjectOfSameSubdomain(CheckedProject project, String dependencyPath) {
        project.roleOfProject(dependencyPath)?.api &&
                project.subdomainOfProject(dependencyPath) == project.subdomain
    }

    private static boolean isInfrastructureGroup(String group) {
        group != null && FORBIDDEN_GROUP_PREFIXES.any { group.startsWith(it) }
    }
}
