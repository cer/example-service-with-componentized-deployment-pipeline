package io.eventuate.customerservice.customermanagement.sagas;

import io.eventuate.common.testcontainers.EventuateDatabaseContainer;
import io.eventuate.common.testcontainers.EventuateVanillaPostgresContainer;
import io.eventuate.customerservice.customermanagement.api.messaging.commands.ReserveCreditCommand;
import io.eventuate.customerservice.customermanagement.api.messaging.replies.CustomerCreditReserved;
import io.eventuate.customerservice.customermanagement.domain.CreditReservationDetails;
import io.eventuate.customerservice.customermanagement.domain.CustomerService;
import io.eventuate.customerservice.customermanagement.sagas.proxies.CustomerServiceProxy;
import io.eventuate.examples.common.money.Money;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeCluster;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.spring.flyway.EventuateTramFlywayMigrationConfiguration;
import io.eventuate.tram.spring.testing.outbox.commands.CommandOutboxTestSupport;
import io.eventuate.tram.spring.testing.outbox.commands.EnableCommandOutboxTestSupport;
import io.eventuate.tram.testing.producer.kafka.replies.DirectToKafkaCommandReplyProducer;
import io.eventuate.tram.testing.producer.kafka.replies.EnableDirectToKafkaCommandReplyProducer;
import io.eventuate.util.test.async.Eventually;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.lifecycle.Startables;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ReserveCreditSagaIntegrationTest {

    public static EventuateKafkaNativeCluster eventuateKafkaCluster = new EventuateKafkaNativeCluster("customer-management-sagas-tests");

    private static final EventuateDatabaseContainer database = new EventuateVanillaPostgresContainer();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        Startables.deepStart(eventuateKafkaCluster.kafka, database).join();

        Stream.of(database, eventuateKafkaCluster.kafka).forEach(container ->
            container.registerProperties(registry::add)
        );
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableCommandOutboxTestSupport
    @EnableDirectToKafkaCommandReplyProducer
    @Import({CustomerManagementSagasConfiguration.class, EventuateTramFlywayMigrationConfiguration.class})
    static class TestConfiguration {
    }

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private CustomerManagementSagaService customerManagementSagaService;

    @Autowired
    private CommandOutboxTestSupport commandOutboxTestSupport;

    @Autowired
    private DirectToKafkaCommandReplyProducer commandReplyProducer;

    @Test
    void shouldReserveCreditSuccessfully() {
        long customerId = System.currentTimeMillis();
        long orderId = 102L;
        Money orderTotal = new Money("12.34");

        customerManagementSagaService.reserveCredit(customerId, orderId, orderTotal);

        Message commandMessage = commandOutboxTestSupport.assertThatCommandMessageSent(
                ReserveCreditCommand.class, CustomerServiceProxy.CHANNEL,
                cmd -> cmd.getCustomerId() == customerId);

        commandReplyProducer.sendReply(commandMessage, ReserveCreditCommand.class, new CustomerCreditReserved());

        Eventually.eventually(() -> {
            CreditReservationDetails expectedDetails = new CreditReservationDetails(customerId, orderId, orderTotal);
            verify(customerService).noteCreditReservationPending(expectedDetails);
            verify(customerService).noteCreditReservationApproved(expectedDetails);
        });
    }
}
