package io.eventuate.customerservice.customermanagement.eventpublishing;

import io.eventuate.common.testcontainers.EventuateVanillaPostgresContainer;
import io.eventuate.examples.common.money.Money;
import io.eventuate.customerservice.customermanagement.domain.Customer;
import io.eventuate.customerservice.customermanagement.domain.CustomerCreditReservedEvent;
import io.eventuate.customerservice.customermanagement.domain.CustomerEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.lifecycle.Startables;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = CustomerEventPublishingIntegrationTest.Config.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CustomerEventPublishingIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @Import({CustomerEventPublishingConfiguration.class})
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
    private JdbcTemplate jdbcTemplate;

    private void setId(Object entity, Object id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    @Test
    public void shouldPublishCustomerCreditReservedEventToOutbox() throws Exception {
        long customerId = System.currentTimeMillis();
        long orderId = 99L;

        Customer customer = new Customer("John Doe", new Money("500.00"));
        setId(customer, customerId);

        customerEventPublisher.publish(customer, new CustomerCreditReservedEvent(orderId));

        String destination = Customer.class.getName();
        List<Map<String, Object>> messages = jdbcTemplate.queryForList(
                "SELECT * FROM message WHERE destination = ? AND headers LIKE ?",
                destination, "%" + CustomerCreditReservedEvent.class.getName() + "%");

        assertFalse(messages.isEmpty(), "Expected at least one event in the outbox for destination " + destination);
        String payload = (String) messages.get(0).get("payload");
        assertTrue(payload.contains(String.valueOf(orderId)),
                "Expected outbox payload to contain orderId " + orderId);
    }
}
