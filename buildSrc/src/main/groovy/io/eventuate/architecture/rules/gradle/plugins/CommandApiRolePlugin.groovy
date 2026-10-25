package io.eventuate.architecture.rules.gradle.plugins

import io.eventuate.architecture.rules.gradle.roles.Role

class CommandApiRolePlugin extends IntegrationTestedRolePlugin {

    CommandApiRolePlugin() {
        super(Role.COMMAND_API)
    }
}
