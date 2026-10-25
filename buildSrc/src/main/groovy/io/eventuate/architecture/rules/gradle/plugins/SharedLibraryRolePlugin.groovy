package io.eventuate.architecture.rules.gradle.plugins

import io.eventuate.architecture.rules.gradle.roles.Role

class SharedLibraryRolePlugin extends RolePlugin {

    SharedLibraryRolePlugin() {
        super(Role.SHARED_LIBRARY)
    }
}
