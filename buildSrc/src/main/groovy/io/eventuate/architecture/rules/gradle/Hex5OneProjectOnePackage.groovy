package io.eventuate.architecture.rules.gradle

class Hex5OneProjectOnePackage implements ProjectDependencyRule {

    @Override
    List<RuleViolation> check(CheckedProject project) {
        MainJavaSources sources = project.mainJavaSources
        sources.filesOutsideDeclaredPackages().collect {
            new RuleViolation(Rule.HEX_5,
                    "${project.path} has ${it.path}, which is outside its package ${sources.declaredPackages.describe()}")
        }
    }
}
