package io.eventuate.architecture.rules.gradle.tasks

import io.eventuate.architecture.rules.gradle.project.ArchitectureRulesExtension
import io.eventuate.architecture.rules.gradle.project.RulesProjectConfigurations
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test

// Registers checkArchitectureRules, which runs the rules project's ArchUnit tests against this project's main classes
class CheckArchitectureRules {

    static final String NAME = 'checkArchitectureRules'
    private static final String SYSTEM_PROPERTY_PREFIX = 'architectureRules.'

    static void register(Project project, ArchitectureRulesExtension extension) {
        Configuration rulesClasses = rulesProjectConfiguration(project, RulesProjectConfigurations.CLASSES, LibraryElements.CLASSES)
        rulesClasses.transitive = false
        Configuration rulesRuntime = rulesProjectConfiguration(project, RulesProjectConfigurations.RUNTIME, LibraryElements.JAR)
        SourceSet main = project.extensions.getByType(SourceSetContainer).getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        def checkArchitectureRules = project.tasks.register(NAME, Test) {
            it.group = 'verification'
            it.description = 'Checks the classes of this project against the ArchUnit architecture rules.'
            it.useJUnitPlatform()
            it.testClassesDirs = rulesClasses
            it.classpath = rulesRuntime + main.output
            it.systemProperties(systemPropertiesOf(project, extension, main))
        }
        project.tasks.named('check') { it.dependsOn(checkArchitectureRules) }
        project.tasks.named('test') { it.mustRunAfter(checkArchitectureRules) }
    }

    private static Map<String, String> systemPropertiesOf(Project project, ArchitectureRulesExtension extension, SourceSet main) {
        [
                role      : extension.role.get().roleName,
                group     : project.group.toString(),
                subdomain : extension.subdomain.getOrElse(''),
                package   : extension.projectPackage.get(),
                classesDir: main.java.destinationDirectory.get().asFile.absolutePath,
        ].collectEntries { name, value -> [(SYSTEM_PROPERTY_PREFIX + name): value] }
    }

    // The rules project is looked up when the configuration is resolved, so a build that never runs the task needs no rules project
    private static Configuration rulesProjectConfiguration(Project project, String name, String libraryElements) {
        project.configurations.create(name) { Configuration configuration ->
            configuration.canBeConsumed = false
            configuration.canBeResolved = true
            configuration.attributes {
                it.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME))
                it.attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category, Category.LIBRARY))
                it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements, libraryElements))
            }
            configuration.withDependencies {
                it.add(project.dependencies.project(path: rulesProjectPath(project)))
            }
        }
    }

    private static String rulesProjectPath(Project project) {
        project.rootProject.extensions.getByType(ArchitectureRulesExtension).rulesProject.get()
    }
}
