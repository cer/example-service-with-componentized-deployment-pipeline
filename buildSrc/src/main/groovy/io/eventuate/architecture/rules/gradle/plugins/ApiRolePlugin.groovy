package io.eventuate.architecture.rules.gradle.plugins

import io.eventuate.architecture.rules.gradle.roles.Role

class ApiRolePlugin extends RolePlugin {

    ApiRolePlugin() {
        super(Role.API)
    }
}
