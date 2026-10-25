package io.eventuate.architecture.rules.gradle.tasks

import io.eventuate.architecture.rules.gradle.project.SubdomainGraph
import io.eventuate.architecture.rules.gradle.rules.Rule
import io.eventuate.architecture.rules.gradle.rules.RuleViolation
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class CheckSubdomainGraphTask extends DefaultTask {

    static final String NAME = 'checkSubdomainGraph'

    @Internal
    abstract Property<SubdomainGraph> getSubdomainGraph()

    @TaskAction
    void checkSubdomainGraph() {
        List<String> cycle = subdomainGraph.get().findCycle()
        if (cycle) {
            throw new GradleException(new RuleViolation(Rule.SUB_6,
                    "the subdomains depend on each other's API projects in the cycle ${cycle.join(' -> ')}").toString())
        }
    }
}
