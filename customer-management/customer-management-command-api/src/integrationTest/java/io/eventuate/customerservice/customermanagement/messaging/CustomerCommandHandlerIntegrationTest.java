package io.eventuate.customerservice.customermanagement.messaging;

import io.eventuate.common.testcontainers.EventuateDatabaseContainer;
import io.eventuate.common.testcontainers.EventuateVanillaPostgresContainer;
import io.eventuate.customerservice.customermanagement.api.messaging.commands.ReserveCreditCommand;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.examples.common.money.Money;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeCluster;
import io.eventuate.tram.spring.flyway.EnableEventuateTramFlywayMigration;
import io.eventuate.tram.spring.testing.outbox.commands.CommandOutboxTestSupport;
import io.eventuate.tram.spring.testing.outbox.commands.EnableCommandOutboxTestSupport;
import io.eventuate.tram.testing.producer.kafka.commands.DirectToKafkaCommandProducer;
import io.eventuate.tram.testing.producer.kafka.commands.EnableDirectToKafkaCommandProducer;
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

import java.util.Collections;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CustomerCommandHandlerIntegrationTest {

  public static EventuateKafkaNativeCluster eventuateKafkaCluster = new EventuateKafkaNativeCluster("customer-service-tests");

  private static final EventuateDatabaseContainer database = new EventuateVanillaPostgresContainer();

  @DynamicPropertySource
  static void registerDbProperties(DynamicPropertyRegistry registry) {
    Startables.deepStart(eventuateKafkaCluster.kafka, database).join();

    Stream.of(database, eventuateKafkaCluster.kafka).forEach(container -> {
      container.registerProperties(registry::add);
    });
  }

  @Configuration
  @EnableAutoConfiguration
  @EnableDirectToKafkaCommandProducer
  @EnableCommandOutboxTestSupport
  @EnableEventuateTramFlywayMigration
  @Import(CustomerMessagingConfiguration.class)
  static public class Config {

  }

  @MockitoBean
  private CustomerManagementService customerManagementService;

  @Autowired
  private DirectToKafkaCommandProducer commandProducer;

  @Autowired
  private CommandOutboxTestSupport commandOutboxTestSupport;

  @Test
  public void shouldHandleReserveCreditCommand() {

    String replyTo = "my-reply-to-channel-" + System.currentTimeMillis();

    long customerId = System.currentTimeMillis();
    long orderId = 102L;
    Money orderTotal = new Money("12.34");

    sendCommand(customerId, orderId, orderTotal, replyTo);

    Eventually.eventually(() -> {

      verify(customerManagementService).reserveCredit(customerId, orderId, orderTotal);

      commandOutboxTestSupport.assertCommandReplyMessageSent(replyTo);
    });
  }

  private void sendCommand(long customerId, long orderId, Money orderTotal, String replyTo) {
    commandProducer.send("customerService", new ReserveCreditCommand(customerId, orderId, orderTotal), replyTo, Collections.emptyMap());
  }


}
