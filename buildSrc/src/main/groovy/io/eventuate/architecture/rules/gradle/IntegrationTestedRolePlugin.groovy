package io.eventuate.architecture.rules.gradle

import org.gradle.api.Project

abstract class IntegrationTestedRolePlugin extends RolePlugin {

    protected IntegrationTestedRolePlugin(Role role) {
        super(role)
    }

    @Override
    protected void applyConventions(Project project) {
        project.pluginManager.apply(INTEGRATION_TESTS_PLUGIN_ID)
    }
}
