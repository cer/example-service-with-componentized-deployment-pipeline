package io.eventuate.customerservice.customermanagement.eventpublishing;

import io.eventuate.customerservice.customermanagement.domain.CustomerEventPublisher;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.spring.flyway.EnableEventuateTramFlywayMigration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableEventuateTramFlywayMigration
public class CustomerManagementEventPublishingConfiguration {

    @Bean
    public CustomerEventPublisher customerEventPublisher(DomainEventPublisher domainEventPublisher) {
        return new CustomerEventPublisherImpl(domainEventPublisher);
    }
}
