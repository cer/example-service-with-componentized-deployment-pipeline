package io.eventuate.architecture.rules.gradle

import org.gradle.api.Project
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class Hex5OneProjectOnePackageTest {

    private static final String DOMAIN_PACKAGE_PATH = 'io/example/service/a/domain'
    private static final String OTHER_PACKAGE_PATH = 'io/example/service/a/restapi'
    private static final String COPY_PACKAGE = 'io.example.otherservice.b.domain'

    private final RuleFixtureBuild build = new RuleFixtureBuild()

    @Test
    void fileInProjectPackagePasses() {
        Project domain = build.subdomainProject('a', Role.DOMAIN)
        build.mainJavaSourceFile(domain, "${DOMAIN_PACKAGE_PATH}/Customer.java")
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(domain))
    }

    @Test
    void fileInSubPackagePasses() {
        Project domain = build.subdomainProject('a', Role.DOMAIN)
        build.mainJavaSourceFile(domain, "${DOMAIN_PACKAGE_PATH}/events/CustomerCreated.java")
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(domain))
    }

    @Test
    void fileInAnotherPackageFails() {
        Project domain = build.subdomainProject('a', Role.DOMAIN)
        build.mainJavaSourceFile(domain, "${OTHER_PACKAGE_PATH}/Foo.java")
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(domain)

        assertTrue(failure.startsWith('HEX-5 (one project, one package)'), failure)
        assertTrue(failure.contains("src/main/java/${OTHER_PACKAGE_PATH}/Foo.java"), failure)
    }

    @Test
    void fileInDeclaredCopyPackagePasses() {
        Project domain = build.subdomainProject('a', Role.DOMAIN)
        domain.extensions.getByType(ArchitectureRulesExtension).messageCopyPackages.set([COPY_PACKAGE])
        build.mainJavaSourceFile(domain, 'io/example/otherservice/b/domain/OtherEvent.java')
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(domain))
    }
}
