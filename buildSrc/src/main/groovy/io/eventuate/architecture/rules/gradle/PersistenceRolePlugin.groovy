package io.eventuate.architecture.rules.gradle

class PersistenceRolePlugin extends IntegrationTestedRolePlugin {

    PersistenceRolePlugin() {
        super(Role.PERSISTENCE)
    }
}
