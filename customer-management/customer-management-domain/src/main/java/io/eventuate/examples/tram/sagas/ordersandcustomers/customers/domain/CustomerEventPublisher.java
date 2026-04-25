package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain;

import io.eventuate.tram.events.publisher.DomainEventPublisherForAggregate;

public interface CustomerEventPublisher
    extends DomainEventPublisherForAggregate<Customer, Long, CustomerEvent> {}
