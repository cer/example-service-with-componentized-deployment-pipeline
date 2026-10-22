package io.eventuate.architecture.rules.gradle

import groovy.io.FileType
import groovy.transform.Immutable
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

// A file in a main source set's Java source directory, whose package is given by its path, not its contents
@Immutable
class MainJavaSourceFile {

    // Relative to the project directory, such as src/main/java/io/example/Foo.java
    String path
    String packageName

    private static Set<File> sourceDirectoriesOf(Project project) {
        SourceSetContainer sourceSets = project.extensions.findByType(SourceSetContainer)
        sourceSets?.findByName(SourceSet.MAIN_SOURCE_SET_NAME)?.java?.srcDirs ?: [] as Set
    }

    static List<MainJavaSourceFile> of(Project project) {
        sourceDirectoriesOf(project)
                .findAll { it.directory }
                .collectMany { filesIn(project.projectDir, it) }
    }

    private static List<MainJavaSourceFile> filesIn(File projectDir, File sourceDirectory) {
        List<MainJavaSourceFile> files = []
        sourceDirectory.eachFileRecurse(FileType.FILES) { File file ->
            files << new MainJavaSourceFile(relativePath(projectDir, file), packageOf(sourceDirectory, file))
        }
        files.sort { it.path }
    }

    private static String packageOf(File sourceDirectory, File file) {
        relativePath(sourceDirectory, file.parentFile).replace('/', '.')
    }

    private static String relativePath(File from, File to) {
        from.toPath().relativize(to.toPath()).toString().replace(File.separator, '/')
    }
}
