package io.eventuate.architecture.rules.gradle

import groovy.transform.Immutable

// The project's package and the SUB-4 message copy packages its build file declares
@Immutable
class DeclaredPackages {

    String projectPackage
    List<String> messageCopyPackages

    static DeclaredPackages of(ArchitectureRulesExtension extension) {
        new DeclaredPackages(extension.projectPackage.getOrNull(), extension.messageCopyPackages.getOrElse([]))
    }

    boolean contains(String packageName) {
        allPackages().any { packageName == it || packageName.startsWith("${it}.") }
    }

    String describe() {
        messageCopyPackages ? "${projectPackage} and its message copy packages ${messageCopyPackages.join(', ')}" : projectPackage
    }

    private List<String> allPackages() {
        [projectPackage, *messageCopyPackages]
    }
}
