package io.eventuate.architecture.rules.gradle

import org.gradle.api.provider.Property

abstract class ArchitectureRulesExtension {

    abstract Property<Role> getRole()

    // The name of the project's parent directory, absent when that is the root directory
    abstract Property<String> getSubdomain()

    abstract Property<String> getProjectPackage()
}
