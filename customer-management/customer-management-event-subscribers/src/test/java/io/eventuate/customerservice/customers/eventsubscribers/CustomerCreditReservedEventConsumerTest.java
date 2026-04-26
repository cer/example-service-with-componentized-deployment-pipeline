package io.eventuate.customerservice.customers.eventsubscribers;

import io.eventuate.customerservice.customers.domain.CustomerCreditReservedEvent;
import io.eventuate.customerservice.customers.domain.CustomerService;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.spring.inmemory.TramInMemoryConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class CustomerCreditReservedEventConsumerTest {

    @Configuration
    @EnableAutoConfiguration
    @Import({CustomerEventSubscribersConfiguration.class, TramInMemoryConfiguration.class})
    static class Config {

    }

    @Autowired
    private DomainEventPublisher domainEventPublisher;

    @MockitoBean
    private CustomerService customerService;

    @Test
    public void shouldConsumeCustomerCreditReservedEvent() {
        CustomerCreditReservedEvent event = new CustomerCreditReservedEvent(99L);

        domainEventPublisher.publish(
            "io.eventuate.customerservice.customers.domain.Customer",
            "1",
            Collections.singletonList(event));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(customerService).noteCreditReserved("1", 99L)
        );
    }
}
