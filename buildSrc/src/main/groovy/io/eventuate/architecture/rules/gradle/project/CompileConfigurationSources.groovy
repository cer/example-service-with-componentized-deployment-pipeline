package io.eventuate.architecture.rules.gradle.project

import groovy.io.FileType
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

import java.util.regex.Pattern

// The Java source directories of each source set, keyed by the configurations that put a dependency on its compile
// classpath, such as implementation and api for main and componentTestImplementation for componentTest
class CompileConfigurationSources {

    private final Map<String, Set<File>> sourceDirectoriesByConfiguration

    CompileConfigurationSources(Map<String, Set<File>> sourceDirectoriesByConfiguration) {
        this.sourceDirectoriesByConfiguration = sourceDirectoriesByConfiguration
    }

    static CompileConfigurationSources of(Project project) {
        new CompileConfigurationSources(sourceSetsOf(project).collectEntries { SourceSet sourceSet ->
            Set<File> sourceDirectories = writtenSourceDirectoriesOf(project, sourceSet)
            compileConfigurationNames(sourceSet).collectEntries { [(it): sourceDirectories] }
        })
    }

    static Set<File> javaSourceDirectoriesOf(Project project) {
        sourceSetsOf(project).collectMany { writtenSourceDirectoriesOf(project, it) } as Set
    }

    // Excludes directories that tasks generate under build, such as generateContractTests' output, since the checks
    // run without depending on any task
    private static Set<File> writtenSourceDirectoriesOf(Project project, SourceSet sourceSet) {
        File srcDirectory = project.file('src')
        sourceSet.java.srcDirs.findAll { it.toPath().startsWith(srcDirectory.toPath()) } as Set
    }

    boolean isCompileConfiguration(String configuration) {
        sourceDirectoriesByConfiguration.containsKey(configuration)
    }

    // True when a Java file of the configuration's source set contains one of the package names, not followed by
    // more of an identifier, so that a.commandapi does not match a.commandapiextra
    boolean sourcesMentionAnyOf(String configuration, List<String> packageNames) {
        Pattern packageNamePattern = Pattern.compile(packageNames.collect { Pattern.quote(it) + /(?![\w$])/ }.join('|'))
        javaFilesOf(configuration).any { packageNamePattern.matcher(it.text).find() }
    }

    private List<File> javaFilesOf(String configuration) {
        List<File> files = []
        sourceDirectoriesByConfiguration[configuration].findAll { it.directory }.each {
            it.eachFileRecurse(FileType.FILES) { File file ->
                if (file.name.endsWith('.java')) {
                    files << file
                }
            }
        }
        files
    }

    private static List<String> compileConfigurationNames(SourceSet sourceSet) {
        [sourceSet.implementationConfigurationName, sourceSet.apiConfigurationName]
    }

    private static List<SourceSet> sourceSetsOf(Project project) {
        project.extensions.findByType(SourceSetContainer)?.toList() ?: []
    }
}
