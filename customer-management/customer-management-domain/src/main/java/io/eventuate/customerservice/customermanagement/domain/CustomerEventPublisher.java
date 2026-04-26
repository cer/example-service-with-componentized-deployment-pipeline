package io.eventuate.customerservice.customermanagement.domain;

import io.eventuate.tram.events.publisher.DomainEventPublisherForAggregate;

public interface CustomerEventPublisher
    extends DomainEventPublisherForAggregate<Customer, Long, CustomerEvent> {}
