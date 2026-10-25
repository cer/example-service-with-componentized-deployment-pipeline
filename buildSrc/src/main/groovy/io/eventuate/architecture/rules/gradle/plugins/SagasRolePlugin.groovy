package io.eventuate.architecture.rules.gradle.plugins

import io.eventuate.architecture.rules.gradle.roles.Role

class SagasRolePlugin extends IntegrationTestedRolePlugin {

    SagasRolePlugin() {
        super(Role.SAGAS)
    }
}
