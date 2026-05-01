package io.eventuate.customerservice.customermanagement.eventsubscribers;

import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.otherservice.othersubdomain.domain.OtherEvent;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.spring.inmemory.EnableTramInMemory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=" // Otherwise, Error creating bean with name 'eventuateCommonJdbcOperations' could not resolve placeholder 'spring.datasource.driver-class-name'
})
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
    public void shouldConsumeOtherEvent() {
        OtherEvent event = new OtherEvent(99L);

        domainEventPublisher.publish(
            "io.eventuate.otherservice.othersubdomain.domain.OtherAggregate",
            "1",
            Collections.singletonList(event));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(customerManagementService).noteCreditReserved("1", 99L)
        );
    }
}
