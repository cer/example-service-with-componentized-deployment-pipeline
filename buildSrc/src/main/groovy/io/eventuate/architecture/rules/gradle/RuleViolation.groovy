package io.eventuate.architecture.rules.gradle

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
