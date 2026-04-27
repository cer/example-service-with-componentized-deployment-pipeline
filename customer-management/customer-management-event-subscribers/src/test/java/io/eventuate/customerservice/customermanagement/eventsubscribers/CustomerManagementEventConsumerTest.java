package io.eventuate.customerservice.customermanagement.eventsubscribers;

import io.eventuate.customerservice.customermanagement.domain.CustomerCreditReservedEvent;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.spring.inmemory.EnableTramInMemory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class CustomerManagementEventConsumerTest {

    @Configuration
    @EnableAutoConfiguration
    @EnableTramInMemory
    @Import(CustomerManagementEventConsumerConfiguration.class)
    static class Config {

    }

    @Autowired
    private DomainEventPublisher domainEventPublisher;

    @MockitoBean
    private CustomerManagementService customerManagementService;

    @Test
    public void shouldConsumeCustomerCreditReservedEvent() {
        CustomerCreditReservedEvent event = new CustomerCreditReservedEvent(99L);

        domainEventPublisher.publish(
            "io.eventuate.customerservice.customermanagement.domain.Customer",
            "1",
            Collections.singletonList(event));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(customerManagementService).noteCreditReserved("1", 99L)
        );
    }
}
