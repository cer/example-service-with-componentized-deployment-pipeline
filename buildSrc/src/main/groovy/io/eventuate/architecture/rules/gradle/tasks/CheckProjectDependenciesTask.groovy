package io.eventuate.architecture.rules.gradle.tasks

import io.eventuate.architecture.rules.gradle.project.CheckedProject
import io.eventuate.architecture.rules.gradle.rules.Hex1DomainDoesNotDependOnAdapters
import io.eventuate.architecture.rules.gradle.rules.Hex2AdaptersDependOnlyOnDomain
import io.eventuate.architecture.rules.gradle.rules.Hex4DomainDependencyList
import io.eventuate.architecture.rules.gradle.rules.Hex5OneProjectOnePackage
import io.eventuate.architecture.rules.gradle.rules.Hex7MainContainsNoBusinessLogic
import io.eventuate.architecture.rules.gradle.rules.Hex8ApiProjectDependencies
import io.eventuate.architecture.rules.gradle.rules.Hex9MessageBrokerInMainOnly
import io.eventuate.architecture.rules.gradle.rules.ProjectDependencyRule
import io.eventuate.architecture.rules.gradle.rules.RuleViolation
import io.eventuate.architecture.rules.gradle.rules.Sub2DependenciesBetweenSubdomainsAreOnApiProjects
import io.eventuate.architecture.rules.gradle.rules.Sub3FourInteractionMechanisms
import io.eventuate.architecture.rules.gradle.rules.Sub7MainIsOnlyCompositionRoot
import io.eventuate.architecture.rules.gradle.rules.Sub8SharedLibraryProjectDependencies
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
            new Hex7MainContainsNoBusinessLogic(),
            new Hex8ApiProjectDependencies(),
            new Hex9MessageBrokerInMainOnly(),
            new Sub2DependenciesBetweenSubdomainsAreOnApiProjects(),
            new Sub3FourInteractionMechanisms(),
            new Sub7MainIsOnlyCompositionRoot(),
            new Sub8SharedLibraryProjectDependencies(),
    ]

    @Internal
    abstract Property<CheckedProject> getCheckedProject()

    // Read by HEX-5 (one project, one package) and HEX-7 (main contains no business logic) through checkedProject
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract ConfigurableFileCollection getJavaSourceDirectories()

    @TaskAction
    void checkProjectDependencies() {
        List<RuleViolation> violations = RULES.collectMany { it.check(checkedProject.get()) }
        if (violations) {
            throw new GradleException(violations.join('\n'))
        }
    }
}
