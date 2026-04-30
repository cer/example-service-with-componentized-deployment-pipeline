package io.eventuate.customerservice.customermanagement.eventsubscribers;

import io.eventuate.common.testcontainers.EventuateDatabaseContainer;
import io.eventuate.common.testcontainers.EventuateVanillaPostgresContainer;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.otherservice.othersubdomain.domain.OtherEvent;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeCluster;
import io.eventuate.tram.spring.flyway.EnableEventuateTramFlywayMigration;
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
public class CustomerManagementEventConsumerIntegrationTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static EventuateKafkaNativeCluster eventuateKafkaCluster = new EventuateKafkaNativeCluster("customer-service-tests");

    private static EventuateDatabaseContainer database = new EventuateVanillaPostgresContainer();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        Startables.deepStart(eventuateKafkaCluster.kafka, database).join();

        Stream.of(database, eventuateKafkaCluster.kafka).forEach(container -> {
            container.registerProperties(registry::add);
        });
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableEventuateTramFlywayMigration
    @Import(CustomerManagementEventConsumerConfiguration.class)
    @EnableDirectToKafkaDomainEventPublisher
    static class Config {

    }

    @Autowired
    private DirectToKafkaDomainEventPublisher domainEventPublisher;

    @Autowired
    private CustomerManagementEventConsumer customerManagementEventConsumer;

    // What about this:
    // This is required because of testImplementation 'io.eventuate.tram.core:eventuate-tram-spring-events-publisher-starter'
    //    @MockitoBean
    //    private MessageProducer messageProducer;


    @MockitoBean
    private CustomerManagementService customerManagementService;

    @Test
    public void shouldConsumeOtherEvent() throws InterruptedException {

        OtherEvent event = new OtherEvent(99L);

        logger.info("Publishing OtherEvent with orderId 99");

        domainEventPublisher.publish(
                "io.eventuate.otherservice.othersubdomain.domain.OtherAggregate",
                "1",
                event);

        logger.info("Published OtherEvent with orderId 99");

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(customerManagementService).noteCreditReserved("1", 99L)
        );
    }
}
