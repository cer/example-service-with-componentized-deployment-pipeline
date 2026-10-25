package io.eventuate.architecture.rules.gradle.rules

import io.eventuate.architecture.rules.gradle.RuleFixtureBuild
import io.eventuate.architecture.rules.gradle.roles.Role
import org.gradle.api.Project
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class Sub7MainIsOnlyCompositionRootTest {

    private final RuleFixtureBuild build = new RuleFixtureBuild()

    @Test
    void sharedLibraryOnAdaptersOfTwoSubdomainsFails() {
        Project sharedLibrary = build.projectWithRole(build.rootProject, 'common', Role.SHARED_LIBRARY)
        build.dependsOn(sharedLibrary, 'implementation', build.subdomainProject('a', Role.REST_API))
        build.dependsOn(sharedLibrary, 'implementation', build.subdomainProject('b', Role.PERSISTENCE))
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(sharedLibrary)

        assertTrue(failure.startsWith('SUB-7 (main is the only composition root)'), failure)
        assertTrue(failure.contains(':a:a-rest-api'), failure)
        assertTrue(failure.contains(':b:b-persistence'), failure)
    }

    @Test
    void dependencyOnMainFails() {
        Project main = build.projectWithRole(build.rootProject, 'x-main', Role.MAIN)
        Project sharedLibrary = build.projectWithRole(build.rootProject, 'common', Role.SHARED_LIBRARY)
        build.dependsOn(sharedLibrary, 'testImplementation', main)
        build.evaluate()

        String failure = build.checkProjectDependenciesFailure(sharedLibrary)

        assertTrue(failure.startsWith('SUB-7 (main is the only composition root)'), failure)
        assertTrue(failure.contains("testImplementation project(':x-main')"), failure)
    }

    @Test
    void mainOnAdaptersOfTwoSubdomainsPasses() {
        Project main = build.projectWithRole(build.rootProject, 'x-main', Role.MAIN)
        build.dependsOn(main, 'runtimeOnly', build.subdomainProject('a', Role.REST_API))
        build.dependsOn(main, 'runtimeOnly', build.subdomainProject('b', Role.PERSISTENCE))
        build.evaluate()

        assertNull(build.checkProjectDependenciesFailure(main))
    }
}
