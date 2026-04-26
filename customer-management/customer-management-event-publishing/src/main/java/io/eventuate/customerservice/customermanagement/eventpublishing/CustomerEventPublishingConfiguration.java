package io.eventuate.customerservice.customermanagement.eventpublishing;

import io.eventuate.customerservice.customermanagement.domain.CustomerEventPublisher;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.spring.flyway.EventuateTramFlywayMigrationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateTramFlywayMigrationConfiguration.class)
public class CustomerEventPublishingConfiguration {

    @Bean
    public CustomerEventPublisher customerEventPublisher(DomainEventPublisher domainEventPublisher) {
        return new CustomerEventPublisherImpl(domainEventPublisher);
    }
}
