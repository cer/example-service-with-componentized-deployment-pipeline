package io.eventuate.architecture.rules.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class ArchitectureRulesPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = 'architectureRules'

    @Override
    void apply(Project project) {
        project.extensions.create(EXTENSION_NAME, ArchitectureRulesExtension)
    }
}
