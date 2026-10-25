package io.eventuate.architecture.rules.gradle.rules

import io.eventuate.architecture.rules.gradle.project.CheckedProject

interface ProjectDependencyRule {

    List<RuleViolation> check(CheckedProject project)
}
