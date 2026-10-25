package io.eventuate.architecture.rules.gradle.plugins

import io.eventuate.architecture.rules.gradle.roles.Role

class RestApiRolePlugin extends IntegrationTestedRolePlugin {

    RestApiRolePlugin() {
        super(Role.REST_API)
    }
}
