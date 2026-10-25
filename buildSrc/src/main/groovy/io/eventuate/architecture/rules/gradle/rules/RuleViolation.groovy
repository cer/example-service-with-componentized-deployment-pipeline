package io.eventuate.architecture.rules.gradle.rules

import groovy.transform.Immutable

@Immutable
class RuleViolation {

    Rule rule
    String message

    @Override
    String toString() {
        "${rule}: ${message}"
    }
}
