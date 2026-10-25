package io.eventuate.architecture.rules.gradle.plugins

import io.eventuate.architecture.rules.gradle.project.ArchitectureRulesExtension
import io.eventuate.architecture.rules.gradle.roles.Role
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class RolePlugin implements Plugin<Project> {

    static final String INTEGRATION_TESTS_PLUGIN_ID = 'io.eventuate.plugins.gradle.testing.integration-tests'
    static final String COMPONENT_TESTS_PLUGIN_ID = 'io.eventuate.plugins.gradle.testing.component-tests'

    private final Role role

    protected RolePlugin(Role role) {
        this.role = role
    }

    @Override
    void apply(Project project) {
        project.pluginManager.apply(ArchitectureRulesPlugin)
        project.extensions.getByType(ArchitectureRulesExtension).role.set(role)
        applyConventions(project)
    }

    protected void applyConventions(Project project) {
    }
}
