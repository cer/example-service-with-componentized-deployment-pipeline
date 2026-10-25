package io.eventuate.architecture.rules.gradle

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import org.junit.jupiter.api.Test

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import static com.tngtech.archunit.library.Architectures.layeredArchitecture

// Each layer depends only on the layers below it
class PackageLayersTest {

    private static final String ROOT_PACKAGE = 'io.eventuate.architecture.rules.gradle'
    private static final List<String> LAYERS_FROM_TOP = ['plugins', 'tasks', 'rules', 'project', 'roles']

    private static final JavaClasses MAIN_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(ROOT_PACKAGE)

    @Test
    void everyClassIsInALayer() {
        classes().should().resideInAnyPackage(layerPackages(*LAYERS_FROM_TOP))
                .check(MAIN_CLASSES)
    }

    @Test
    void eachLayerDependsOnlyOnTheLayersBelowIt() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer('plugins').definedBy(layerPackages('plugins'))
                .layer('tasks').definedBy(layerPackages('tasks'))
                .layer('rules').definedBy(layerPackages('rules'))
                .layer('project').definedBy(layerPackages('project'))
                .layer('roles').definedBy(layerPackages('roles'))
                .whereLayer('plugins').mayNotBeAccessedByAnyLayer()
                .whereLayer('tasks').mayOnlyBeAccessedByLayers('plugins')
                .whereLayer('rules').mayOnlyBeAccessedByLayers('plugins', 'tasks')
                .whereLayer('project').mayOnlyBeAccessedByLayers('plugins', 'tasks', 'rules')
                .check(MAIN_CLASSES)
    }

    private static String[] layerPackages(String... layers) {
        layers.collect { "${ROOT_PACKAGE}.${it}..".toString() } as String[]
    }
}
