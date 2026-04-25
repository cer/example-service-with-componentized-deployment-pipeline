package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.eventsubscribers;

import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerCreditReservedEvent;
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
    @Import({TramInMemoryConfiguration.class})
    static class Config {

        @Bean
        @Primary
        public CustomerCreditReservedEventConsumer customerCreditReservedEventConsumer() {
            return spy(new CustomerCreditReservedEventConsumer());
        }
    }

    @Autowired
    private DomainEventPublisher domainEventPublisher;

    @Autowired
    private CustomerCreditReservedEventConsumer customerCreditReservedEventConsumer;

    @Test
    public void shouldConsumeCustomerCreditReservedEvent() {
        CustomerCreditReservedEvent event = new CustomerCreditReservedEvent(99L);

        domainEventPublisher.publish(
            "io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer",
            "1",
            Collections.singletonList(event));

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            verify(customerCreditReservedEventConsumer).handleCustomerCreditReserved(any())
        );
    }
}
