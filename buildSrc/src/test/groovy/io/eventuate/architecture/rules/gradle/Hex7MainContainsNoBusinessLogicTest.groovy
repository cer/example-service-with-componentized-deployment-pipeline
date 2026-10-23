package io.eventuate.architecture.rules.gradle

import org.gradle.api.Project
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class Hex7MainContainsNoBusinessLogicTest {

    private static final String ADAPTER_CLASS = 'io.example.service.a.commandapi.ReserveCreditCommand'
    private static final String COPY_PACKAGE = 'io.example.otherservice.b.domain'

    private final RuleFixtureBuild build = new RuleFixtureBuild()
    private Project main

    @BeforeEach
    void createMain() {
        main = build.projectWithRole(build.rootProject, 'x-main', Role.MAIN)
        writeMainClassImporting('java.util.List')
    }

    @Test
    void implementationOnAdapterThatMainDoesNotReferenceFails() {
        build.dependsOn(main, 'implementation', build.subdomainProject('a', Role.COMMAND_API))
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(main)

        assertTrue(failure.startsWith('HEX-7 (main contains no business logic)'), failure)
        assertTrue(failure.contains("implementation project(':a:a-command-api')"), failure)
    }

    @Test
    void implementationOnAdapterThatMainReferencesPasses() {
        build.dependsOn(main, 'implementation', build.subdomainProject('a', Role.COMMAND_API))
        writeMainClassImporting(ADAPTER_CLASS)
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(main))
    }

    @Test
    void componentTestImplementationOnAdapterThatComponentTestReferencesPasses() {
        build.dependsOn(main, 'componentTestImplementation', build.subdomainProject('a', Role.COMMAND_API))
        writeComponentTestImporting(ADAPTER_CLASS)
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(main))
    }

    @Test
    void componentTestImplementationOnAdapterThatOnlyMainReferencesFails() {
        build.dependsOn(main, 'componentTestImplementation', build.subdomainProject('a', Role.COMMAND_API))
        writeMainClassImporting(ADAPTER_CLASS)
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(main)

        assertTrue(failure.startsWith('HEX-7 (main contains no business logic)'), failure)
        assertTrue(failure.contains("componentTestImplementation project(':a:a-command-api')"), failure)
    }

    @Test
    void componentTestImplementationOnAdapterWhoseCopyPackageComponentTestReferencesPasses() {
        Project eventSubscribers = build.subdomainProject('a', Role.EVENT_SUBSCRIBERS)
        eventSubscribers.extensions.getByType(ArchitectureRulesExtension).messageCopyPackages.set([COPY_PACKAGE])
        build.dependsOn(main, 'componentTestImplementation', eventSubscribers)
        writeComponentTestImporting("${COPY_PACKAGE}.OtherEvent")
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(main))
    }

    @Test
    void referenceToPackageWithSamePrefixDoesNotCount() {
        build.dependsOn(main, 'implementation', build.subdomainProject('a', Role.COMMAND_API))
        writeMainClassImporting('io.example.service.a.commandapiextra.Foo')
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(main)

        assertTrue(failure.startsWith('HEX-7 (main contains no business logic)'), failure)
    }

    @Test
    void runtimeOnlyOnAdapterPasses() {
        build.dependsOn(main, 'runtimeOnly', build.subdomainProject('a', Role.COMMAND_API))
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(main))
    }

    @Test
    void classInSubPackageOfRootPackageFails() {
        build.mainJavaSourceFile(main, 'io/example/service/a/domain/Customer.java')
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(main)

        assertTrue(failure.startsWith('HEX-7 (main contains no business logic)'), failure)
        assertTrue(failure.contains('src/main/java/io/example/service/a/domain/Customer.java'), failure)
    }

    private void writeMainClassImporting(String importedClass) {
        writeRootPackageClass(main.file('src/main/java/io/example/service/XMain.java'), importedClass)
    }

    private void writeComponentTestImporting(String importedClass) {
        writeRootPackageClass(main.file('src/componentTest/java/io/example/service/XComponentTest.java'), importedClass)
    }

    private static void writeRootPackageClass(File file, String importedClass) {
        file.parentFile.mkdirs()
        file.text = "package io.example.service;\n\nimport ${importedClass};\n"
    }
}
