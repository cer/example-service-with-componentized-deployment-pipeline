package io.eventuate.architecture.rules.gradle.plugins

import io.eventuate.architecture.rules.gradle.RuleFixtureBuild
import io.eventuate.architecture.rules.gradle.project.ArchitectureRulesExtension
import io.eventuate.architecture.rules.gradle.roles.Role
import org.gradle.api.Project
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class ProjectNamingTest {

    private final RuleFixtureBuild build = new RuleFixtureBuild()
    private final Project customerManagement = build.subdomainDirectory('customer-management')

    @Test
    void proxyWithoutOtherServiceInItsNameFails() {
        Project project = build.projectWithRole(customerManagement,'customer-proxy', Role.PROXY)

        String failure = build.evaluationFailure(project)

        assertTrue(failure.contains(':customer-management:customer-proxy'), failure)
        assertTrue(failure.contains('does not match the pattern <subdomain>-<other>-proxy'), failure)
    }

    @Test
    void proxyNamedAfterSubdomainAndOtherServicePasses() {
        Project project = build.projectWithRole(customerManagement,'customer-management-other-service-proxy', Role.PROXY)

        assertNull(build.evaluationFailure(project))
    }

    @Test
    void domainProjectAtTheRootFails() {
        Project project = build.projectWithRole(build.rootProject,'x-domain', Role.DOMAIN)

        String failure = build.evaluationFailure(project)

        assertTrue(failure.contains(':x-domain'), failure)
        assertTrue(failure.contains('is not in a subdomain directory'), failure)
    }

    @Test
    void mainProjectAtTheRootPasses() {
        Project project = build.projectWithRole(build.rootProject,'x-main', Role.MAIN)

        assertNull(build.evaluationFailure(project))
    }

    @Test
    void sharedLibraryAtTheRootPasses() {
        Project project = build.projectWithRole(build.rootProject,'common-types', Role.SHARED_LIBRARY)

        assertNull(build.evaluationFailure(project))
    }

    @Test
    void subdomainIsTheParentDirectoryName() {
        Project project = build.projectWithRole(customerManagement,'customer-management-other-service-proxy', Role.PROXY)
        build.evaluate()

        assertEquals('customer-management', extensionOf(project).subdomain.get())
    }

    @Test
    void packageRemovesHyphensFromSubdomainAndRoleSuffix() {
        Project project = build.projectWithRole(customerManagement,'customer-management-other-service-proxy', Role.PROXY)
        build.evaluate()

        assertEquals("${RuleFixtureBuild.ROOT_PACKAGE}.customermanagement.otherserviceproxy".toString(),
                extensionOf(project).projectPackage.get())
    }

    @Test
    void apiFacetPackageNestsTheFacetUnderApi() {
        Project project = build.projectWithRole(customerManagement,'customer-management-api-order-history', Role.API_FACET)
        build.evaluate()

        assertEquals("${RuleFixtureBuild.ROOT_PACKAGE}.customermanagement.api.orderhistory".toString(),
                extensionOf(project).projectPackage.get())
    }

    @Test
    void mainPackageIsTheRootPackage() {
        Project project = build.projectWithRole(build.rootProject,'x-main', Role.MAIN)
        build.evaluate()

        assertEquals(RuleFixtureBuild.ROOT_PACKAGE, extensionOf(project).projectPackage.get())
    }

    private static ArchitectureRulesExtension extensionOf(Project project) {
        project.extensions.getByType(ArchitectureRulesExtension)
    }
}
