package io.eventuate.customerservice.customers.eventpublishing;

import io.eventuate.customerservice.customers.domain.Customer;
import io.eventuate.customerservice.customers.domain.CustomerEvent;
import io.eventuate.customerservice.customers.domain.CustomerEventPublisher;
import io.eventuate.tram.events.publisher.AbstractDomainEventPublisherForAggregateImpl;
import io.eventuate.tram.events.publisher.DomainEventPublisher;

public class CustomerEventPublisherImpl
    extends AbstractDomainEventPublisherForAggregateImpl<Customer, Long, CustomerEvent>
    implements CustomerEventPublisher {

    public CustomerEventPublisherImpl(DomainEventPublisher domainEventPublisher) {
        super(Customer.class, Customer::getId, domainEventPublisher, CustomerEvent.class);
    }
}
