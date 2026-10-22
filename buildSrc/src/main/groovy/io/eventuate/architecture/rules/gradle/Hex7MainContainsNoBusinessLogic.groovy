package io.eventuate.architecture.rules.gradle

import io.eventuate.architecture.rules.gradle.DeclaredDependencies.DeclaredProjectDependency

class Hex7MainContainsNoBusinessLogic implements ProjectDependencyRule {

    @Override
    List<RuleViolation> check(CheckedProject project) {
        if (project.role != Role.MAIN) {
            return []
        }
        unreferencedCompileDependenciesOnAdapters(project) + filesOutsideRootPackage(project)
    }

    private static List<RuleViolation> unreferencedCompileDependenciesOnAdapters(CheckedProject project) {
        project.dependencies.projectDependencies
                .findAll { isCompileDependencyOnAdapter(project, it) && !isReferencedBySourceSet(project, it) }
                .collect {
                    new RuleViolation(Rule.HEX_7,
                            "${project.path} declares ${it.configuration} project('${it.path}'), but no Java file of that source set mentions ${project.declaredPackagesOfProject(it.path).describe()}. Use runtimeOnly")
                }
    }

    private static boolean isCompileDependencyOnAdapter(CheckedProject project, DeclaredProjectDependency dependency) {
        project.roleOfProject(dependency.path)?.group == RoleGroup.ADAPTER &&
                project.compileConfigurationSources.isCompileConfiguration(dependency.configuration)
    }

    private static boolean isReferencedBySourceSet(CheckedProject project, DeclaredProjectDependency dependency) {
        project.compileConfigurationSources.sourcesMentionAnyOf(dependency.configuration,
                project.declaredPackagesOfProject(dependency.path).allPackages())
    }

    private static List<RuleViolation> filesOutsideRootPackage(CheckedProject project) {
        MainJavaSources sources = project.mainJavaSources
        String rootPackage = sources.declaredPackages.projectPackage
        sources.files.findAll { it.packageName != rootPackage }.collect {
            new RuleViolation(Rule.HEX_7,
                    "${project.path} has ${it.path}, which is not directly in the root package ${rootPackage}")
        }
    }
}
