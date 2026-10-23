package io.eventuate.architecture.rules.gradle

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class ArchitectureRulesExtension {

    abstract Property<Role> getRole()

    // The name of the project's parent directory, absent when that is the root directory
    abstract Property<String> getSubdomain()

    abstract Property<String> getProjectPackage()

    // The SUB-4 consumer-side message copies, declared in the producer's package
    abstract ListProperty<String> getMessageCopyPackages()

    // Set in the root project: the paths of the projects that need no role
    abstract ListProperty<String> getExemptProjects()

    // Set in the root project: the path of the project containing the ArchUnit rules
    abstract Property<String> getRulesProject()
}
