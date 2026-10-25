package io.eventuate.architecture.rules.gradle.plugins

import io.eventuate.architecture.rules.gradle.roles.Role

class ProxyRolePlugin extends IntegrationTestedRolePlugin {

    ProxyRolePlugin() {
        super(Role.PROXY)
    }
}
