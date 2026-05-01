package io.eventuate.customerservice.customermanagement.eventpublishing;

import io.eventuate.common.testcontainers.EventuateVanillaPostgresContainer;
import io.eventuate.examples.common.money.Money;
import io.eventuate.customerservice.customermanagement.domain.Customer;
import io.eventuate.customerservice.customermanagement.domain.CustomerCreatedEvent;
import io.eventuate.customerservice.customermanagement.domain.CustomerCreditReservedEvent;
import io.eventuate.customerservice.customermanagement.domain.CustomerEventPublisher;
import io.eventuate.tram.spring.testing.outbox.events.DomainEventOutboxTestSupport;
import io.eventuate.tram.spring.testing.outbox.events.EnableDomainEventOutboxTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.lifecycle.Startables;

import java.util.UUID;

import static io.eventuate.customerservice.customermanagement.eventpublishing.EntityIdSetter.setId;

@SpringBootTest(classes = CustomerEventPublishingIntegrationTest.Config.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CustomerEventPublishingIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @EnableDomainEventOutboxTestSupport
    @Import({CustomerManagementEventPublishingConfiguration.class})
    static class Config {
    }

    private static final EventuateVanillaPostgresContainer postgres = new EventuateVanillaPostgresContainer();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        Startables.deepStart(postgres).join();
        postgres.registerProperties(registry::add);
    }

    @Autowired
    private CustomerEventPublisher customerEventPublisher;

    @Autowired
    private DomainEventOutboxTestSupport domainEventOutboxTestSupport;

    @Test
    public void shouldPublishCustomerCreditReservedEventToOutbox() {
        UUID customerId = UUID.randomUUID();
        long orderId = 99L;

        Customer customer = new Customer("John Doe", new Money("500.00"));
        setId(customer, customerId);

        customerEventPublisher.publish(customer, new CustomerCreditReservedEvent(orderId));

        domainEventOutboxTestSupport.assertDomainEventInOutbox(
                Customer.class.getName(),
                customerId.toString(),
                CustomerCreditReservedEvent.class.getName());
    }

    @Test
    public void shouldPublishCustomerCreatedEvent() {
        UUID customerId = UUID.randomUUID();

        Customer customer = new Customer("John Doe", new Money("500.00"));
        setId(customer, customerId);

        customerEventPublisher.publish(customer, new CustomerCreatedEvent("John Doe", new Money("500.00")));

        domainEventOutboxTestSupport.assertDomainEventInOutbox(
                Customer.class.getName(),
                customerId.toString(),
                CustomerCreatedEvent.class.getName());
    }
}
