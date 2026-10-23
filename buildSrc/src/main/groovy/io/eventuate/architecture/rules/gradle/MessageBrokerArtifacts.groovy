package io.eventuate.architecture.rules.gradle

// The message broker artifacts of HEX-9 in docs/design/architecture-rules.md, as an allow-list of group and artifact
class MessageBrokerArtifacts {

    private static final List<String> BROKERS = ['kafka', 'activemq', 'rabbitmq', 'redis']

    private static final Set<String> ARTIFACTS = BROKERS.collectMany {
        ["io.eventuate.tram.core:eventuate-tram-spring-jdbc-${it}".toString(),
         "io.eventuate.tram.core:eventuate-tram-spring-consumer-${it}".toString()]
    } as Set

    static boolean isMessageBroker(String group, String name) {
        "${group}:${name}".toString() in ARTIFACTS
    }
}
