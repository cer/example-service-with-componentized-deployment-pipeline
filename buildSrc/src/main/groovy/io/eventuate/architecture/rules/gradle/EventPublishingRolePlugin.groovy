package io.eventuate.architecture.rules.gradle

class EventPublishingRolePlugin extends IntegrationTestedRolePlugin {

    EventPublishingRolePlugin() {
        super(Role.EVENT_PUBLISHING)
    }
}
