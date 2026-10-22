package io.eventuate.architecture.rules.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class CheckProjectDependenciesTask extends DefaultTask {

    static final String NAME = 'checkProjectDependencies'

    // HEX-3 (outbound dependencies go through ports) follows from HEX-1 (the domain does not
    // depend on adapters): a domain project cannot name an adapter's implementing class
    // without a project dependency on the adapter
    private static final List<ProjectDependencyRule> RULES = [
            new Hex1DomainDoesNotDependOnAdapters(),
            new Hex2AdaptersDependOnlyOnDomain(),
            new Hex4DomainDependencyList(),
            new Hex5OneProjectOnePackage(),
            new Sub2DependenciesBetweenSubdomainsAreOnApiProjects(),
    ]

    @Internal
    abstract Property<CheckedProject> getCheckedProject()

    // Read by HEX-5 (one project, one package) through checkedProject
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract ConfigurableFileCollection getMainJavaSourceDirectories()

    @TaskAction
    void checkProjectDependencies() {
        List<RuleViolation> violations = RULES.collectMany { it.check(checkedProject.get()) }
        if (violations) {
            throw new GradleException(violations.join('\n'))
        }
    }
}
