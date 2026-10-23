package io.eventuate.architecture.rules.gradle

import org.gradle.api.Project

// The packages a project declares and the files in its main source set's Java source directories
class MainJavaSources {

    final DeclaredPackages declaredPackages
    final List<MainJavaSourceFile> files

    MainJavaSources(DeclaredPackages declaredPackages, List<MainJavaSourceFile> files) {
        this.declaredPackages = declaredPackages
        this.files = files
    }

    static MainJavaSources of(Project project) {
        new MainJavaSources(DeclaredPackages.of(project.extensions.getByType(ArchitectureRulesExtension)),
                MainJavaSourceFile.of(project))
    }

    List<MainJavaSourceFile> filesOutsideDeclaredPackages() {
        files.findAll { !declaredPackages.contains(it.packageName) }
    }
}
