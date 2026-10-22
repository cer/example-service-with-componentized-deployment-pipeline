package io.eventuate.architecture.rules.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class CheckProjectDependenciesTask extends DefaultTask {

    static final String NAME = 'checkProjectDependencies'

    private static final List<ProjectDependencyRule> RULES = [
            new Hex1DomainDoesNotDependOnAdapters(),
    ]

    @Internal
    abstract Property<CheckedProject> getCheckedProject()

    @TaskAction
    void checkProjectDependencies() {
        List<RuleViolation> violations = RULES.collectMany { it.check(checkedProject.get()) }
        if (violations) {
            throw new GradleException(violations.join('\n'))
        }
    }
}
