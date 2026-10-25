package io.eventuate.customerservice.customermanagement.eventpublishing;

import io.eventuate.customerservice.customermanagement.domain.Customer;
import io.eventuate.customerservice.customermanagement.domain.CustomerCreatedEvent;
import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.spring.testing.cloudcontract.EnableEventuateTramContractVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

/**
 * Base class for the tests that Spring Cloud Contract generates from the contracts in
 * src/contractTest/resources/contracts. Each trigger method publishes the domain event that
 * the corresponding contract specifies.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = BaseClass.TestConfig.class)
public abstract class BaseClass {

    // CustomerManagementEventPublishingConfiguration is not imported because its
    // @EnableEventuateTramFlywayMigration would require a database. Each trigger method builds its
    // publisher directly on the in-memory DomainEventPublisher that @EnableEventuateTramContractVerifier provides.

    @Configuration
    @EnableAutoConfiguration
    @EnableEventuateTramContractVerifier
    public static class TestConfig {
    }

    @Autowired
    private DomainEventPublisher domainEventPublisher;

    public void customerCreatedEvent() {
        // Publishes the event that contracts/client-service/customerCreatedEvent.groovy specifies.
        Customer customer = new Customer("Fred", new Money("15.00"));
        ReflectionTestUtils.setField(customer, "id", UUID.fromString("11111111-1111-1111-1111-111111111111"));
        new CustomerEventPublisherImpl(domainEventPublisher).publish(customer, new CustomerCreatedEvent("Fred", new Money("15.00")));
    }
}
