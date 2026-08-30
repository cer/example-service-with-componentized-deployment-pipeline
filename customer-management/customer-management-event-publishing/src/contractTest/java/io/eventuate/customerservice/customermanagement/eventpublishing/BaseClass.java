package io.eventuate.customerservice.customermanagement.eventpublishing;

import io.eventuate.customerservice.customermanagement.domain.Customer;
import io.eventuate.customerservice.customermanagement.domain.CustomerCreatedEvent;
import io.eventuate.customerservice.customermanagement.domain.CustomerEventPublisher;
import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.spring.testing.cloudcontract.EnableEventuateTramContractVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
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

    public static final String CUSTOMER_ID = "11111111-1111-1111-1111-111111111111";
    public static final String CUSTOMER_NAME = "Fred";
    public static final Money CREDIT_LIMIT = new Money("15.00");

    // CustomerManagementEventPublishingConfiguration is not imported because its
    // @EnableEventuateTramFlywayMigration would require a database. The publisher under test is
    // built directly on the in-memory DomainEventPublisher that @EnableEventuateTramContractVerifier provides.

    @Configuration
    @EnableAutoConfiguration
    @EnableEventuateTramContractVerifier
    public static class TestConfig {

        @Bean
        public CustomerEventPublisher customerEventPublisher(DomainEventPublisher domainEventPublisher) {
            return new CustomerEventPublisherImpl(domainEventPublisher);
        }
    }

    @Autowired
    private CustomerEventPublisher customerEventPublisher;

    public void customerCreatedEvent() {
        customerEventPublisher.publish(existingCustomer(), new CustomerCreatedEvent(CUSTOMER_NAME, CREDIT_LIMIT));
    }

    private static Customer existingCustomer() {
        Customer customer = new Customer(CUSTOMER_NAME, CREDIT_LIMIT);
        ReflectionTestUtils.setField(customer, "id", UUID.fromString(CUSTOMER_ID));
        return customer;
    }
}
