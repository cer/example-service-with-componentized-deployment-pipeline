package io.eventuate.architecture.rules.gradle.plugins

import io.eventuate.architecture.rules.gradle.roles.Role

class EventSubscribersRolePlugin extends IntegrationTestedRolePlugin {

    EventSubscribersRolePlugin() {
        super(Role.EVENT_SUBSCRIBERS)
    }
}
