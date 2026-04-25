package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.eventsubscribers;

import io.eventuate.common.testcontainers.DatabaseContainerFactory;
import io.eventuate.common.testcontainers.EventuateDatabaseContainer;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerCreditReservedEvent;
import io.eventuate.messaging.kafka.producer.EventuateKafkaProducer;
import io.eventuate.messaging.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaCluster;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.eventuate.tram.spring.flyway.EventuateTramFlywayMigrationConfiguration;
import io.eventuate.util.test.async.Eventually;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.lifecycle.Startables;

import java.util.Collections;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "spring.main.allow-bean-definition-overriding=true")
public class CustomerCreditReservedEventConsumerIntegrationTest {

    public static EventuateKafkaCluster eventuateKafkaCluster = new EventuateKafkaCluster();

    private static final EventuateDatabaseContainer database = DatabaseContainerFactory.makeVanillaDatabaseContainer();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        eventuateKafkaCluster.kafka.dependsOn(eventuateKafkaCluster.zookeeper);
        Startables.deepStart(eventuateKafkaCluster.kafka, database).join();

        Stream.of(database, eventuateKafkaCluster.zookeeper, eventuateKafkaCluster.kafka).forEach(container -> {
            container.registerProperties(registry::add);
        });
    }

    @Configuration
    @EnableAutoConfiguration
    @Import({CustomerEventSubscribersConfiguration.class,
            EventuateTramFlywayMigrationConfiguration.class})
    static class Config {

        @Bean
        @Primary
        public CustomerCreditReservedEventConsumer customerCreditReservedEventConsumer() {
            return spy(new CustomerCreditReservedEventConsumer());
        }

        @Bean
        public EventuateKafkaProducer eventuateKafkaProducer(@Value("${eventuatelocal.kafka.bootstrap.servers}") String bootstrapServers) {
            return new EventuateKafkaProducer(bootstrapServers, EventuateKafkaProducerConfigurationProperties.empty());
        }

        @Bean
        @Primary
        public MessageProducerImplementation messageProducerImplementation(EventuateKafkaProducer eventuateKafkaProducer) {
            return new MessageProducerImplementation() {
                @Override
                public void send(Message message) {
                    String destination = message.getRequiredHeader(Message.DESTINATION);
                    eventuateKafkaProducer.send(destination, message.getId(), JSonMapper.toJson(message));
                }
            };
        }
    }

    @Autowired
    private DomainEventPublisher domainEventPublisher;

    @Autowired
    private CustomerCreditReservedEventConsumer customerCreditReservedEventConsumer;

    @Test
    public void shouldConsumeCustomerCreditReservedEvent() {

        Eventually.eventually(() -> {
            CustomerCreditReservedEvent event = new CustomerCreditReservedEvent(99L);

            domainEventPublisher.publish(
                "io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer",
                "1",
                Collections.singletonList(event));

            verify(customerCreditReservedEventConsumer).handleCustomerCreditReserved(any());
        });
    }
}
