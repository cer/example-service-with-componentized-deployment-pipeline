package io.eventuate.architecture.rules.gradle

import org.gradle.api.Project
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class Sub8SharedLibraryProjectDependenciesTest {

    private static final String SUB_8 = 'SUB-8 (shared libraries hold only value types)'

    private final RuleFixtureBuild build = new RuleFixtureBuild()

    @Test
    void sharedLibraryOnSubdomainProjectFails() {
        Project money = sharedLibrary('money')
        build.dependsOn(money, 'implementation', build.subdomainProject('a', Role.API))
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(money)

        assertTrue(failure.startsWith(SUB_8), failure)
        assertTrue(failure.contains(':a:a-api'), failure)
    }

    @Test
    void sharedLibraryOnOtherSharedLibraryFails() {
        Project money = sharedLibrary('money')
        build.dependsOn(money, 'api', sharedLibrary('common'))
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(money)

        assertTrue(failure.startsWith(SUB_8), failure)
        assertTrue(failure.contains(':common'), failure)
    }

    @Test
    void sharedLibraryWithOnlyExternalDependenciesPasses() {
        Project money = sharedLibrary('money')
        build.dependsOn(money, 'implementation', 'org.apache.commons:commons-lang3')
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(money))
    }

    private Project sharedLibrary(String name) {
        build.projectWithRole(build.rootProject, name, Role.SHARED_LIBRARY)
    }
}
