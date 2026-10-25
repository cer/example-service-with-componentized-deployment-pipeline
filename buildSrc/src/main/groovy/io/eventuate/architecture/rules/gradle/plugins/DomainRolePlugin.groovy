package io.eventuate.architecture.rules.gradle.plugins

import io.eventuate.architecture.rules.gradle.roles.Role

class DomainRolePlugin extends RolePlugin {

    DomainRolePlugin() {
        super(Role.DOMAIN)
    }
}
