package io.eventuate.architecture.rules.gradle.plugins

import io.eventuate.architecture.rules.gradle.roles.Role

class EventPublishingRolePlugin extends IntegrationTestedRolePlugin {

    EventPublishingRolePlugin() {
        super(Role.EVENT_PUBLISHING)
    }
}
