package io.eventuate.architecture.rules.gradle

// Every configuration of the integrationTest and componentTest source sets is accepted, because
// customer-management-event-subscribers declares the Kafka consumer integrationTestRuntimeOnly
class Hex9MessageBrokerInMainOnly implements ProjectDependencyRule {

    private static final List<String> BROKER_TEST_SOURCE_SETS = ['integrationTest', 'componentTest']

    @Override
    List<RuleViolation> check(CheckedProject project) {
        if (project.role == Role.MAIN) {
            return []
        }
        project.dependencies.externalDependencies
                .findAll { MessageBrokerArtifacts.isMessageBroker(it.group, it.name) }
                .findAll { !isBrokerTestSourceSetConfiguration(it.configuration) }
                .collect {
                    new RuleViolation(Rule.HEX_9,
                            "${project.path} declares ${it.configuration} \"${it.group}:${it.name}\", which is a message broker artifact that only main or the integrationTest and componentTest source sets declare")
                }
    }

    private static boolean isBrokerTestSourceSetConfiguration(String configuration) {
        BROKER_TEST_SOURCE_SETS.any { configuration.startsWith(it) }
    }
}
