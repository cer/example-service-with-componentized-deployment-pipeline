package io.eventuate.architecture.rules.gradle

import org.gradle.api.provider.Property

abstract class ArchitectureRulesExtension {

    abstract Property<Role> getRole()
}
