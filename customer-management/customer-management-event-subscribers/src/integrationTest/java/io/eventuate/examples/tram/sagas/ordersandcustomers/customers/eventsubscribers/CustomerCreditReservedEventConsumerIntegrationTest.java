package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.eventsubscribers;

import io.eventuate.common.testcontainers.EventuateDatabaseContainer;
import io.eventuate.common.testcontainers.EventuateVanillaPostgresContainer;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerCreditReservedEvent;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerService;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaCluster;
import io.eventuate.tram.spring.flyway.EventuateTramFlywayMigrationConfiguration;
import io.eventuate.tram.testing.producer.kafka.events.DirectToKafkaDomainEventPublisher;
import io.eventuate.tram.testing.producer.kafka.events.EnableDirectToKafkaDomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.lifecycle.Startables;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CustomerCreditReservedEventConsumerIntegrationTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static EventuateKafkaCluster eventuateKafkaCluster = new EventuateKafkaCluster();

//    private static final EventuateDatabaseContainer database = DatabaseContainerFactory.makeVanillaDatabaseContainer();
    private static EventuateDatabaseContainer database = new EventuateVanillaPostgresContainer();

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
    @EnableDirectToKafkaDomainEventPublisher
    static class Config {

    }

    @Autowired
    private DirectToKafkaDomainEventPublisher domainEventPublisher;

    @Autowired
    private CustomerCreditReservedEventConsumer customerCreditReservedEventConsumer;

    // What about this:
    // This is required because of testImplementation 'io.eventuate.tram.core:eventuate-tram-spring-events-publisher-starter'
    //    @MockitoBean
    //    private MessageProducer messageProducer;


    @MockitoBean
    private CustomerService customerService;

    @Test
    public void shouldConsumeCustomerCreditReservedEvent() throws InterruptedException {

        CustomerCreditReservedEvent event = new CustomerCreditReservedEvent(99L);

        logger.info("Publishing CustomerCreditReservedEvent for customer 1 with amount 99");

        domainEventPublisher.publish(
                "io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer",
                "1",
                event);

        logger.info("Published CustomerCreditReservedEvent for customer 1 with amount 99");

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(customerService).noteCreditReserved("1", 99L)
        );
    }
}
