package io.eventuate.architecture.rules.gradle

interface ProjectDependencyRule {

    List<RuleViolation> check(CheckedProject project)
}
