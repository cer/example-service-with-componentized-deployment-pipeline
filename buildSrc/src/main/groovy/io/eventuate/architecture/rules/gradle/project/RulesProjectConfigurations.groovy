package io.eventuate.architecture.rules.gradle.project

import org.gradle.api.artifacts.Configuration

// The configurations through which checkArchitectureRules depends on the rules project, which the project dependency rules do not apply to
class RulesProjectConfigurations {

    static final String CLASSES = 'architectureRulesClasses'
    static final String RUNTIME = 'architectureRulesRuntime'

    static boolean isRulesConfiguration(Configuration configuration) {
        configuration.name in [CLASSES, RUNTIME]
    }
}
