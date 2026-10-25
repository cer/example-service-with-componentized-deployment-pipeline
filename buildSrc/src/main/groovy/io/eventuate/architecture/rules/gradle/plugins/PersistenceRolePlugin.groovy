package io.eventuate.architecture.rules.gradle.plugins

import io.eventuate.architecture.rules.gradle.roles.Role

class PersistenceRolePlugin extends IntegrationTestedRolePlugin {

    PersistenceRolePlugin() {
        super(Role.PERSISTENCE)
    }
}
