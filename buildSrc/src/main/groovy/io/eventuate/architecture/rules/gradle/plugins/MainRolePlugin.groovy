package io.eventuate.architecture.rules.gradle.plugins

import io.eventuate.architecture.rules.gradle.roles.Role
import org.gradle.api.Project

class MainRolePlugin extends RolePlugin {

    MainRolePlugin() {
        super(Role.MAIN)
    }

    @Override
    protected void applyConventions(Project project) {
        project.pluginManager.apply(ServicePlugin)
        project.pluginManager.apply(COMPONENT_TESTS_PLUGIN_ID)
    }
}
