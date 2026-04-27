package io.eventuate.customerservice.customermanagement.eventpublishing;

import io.eventuate.customerservice.customermanagement.domain.Customer;
import io.eventuate.customerservice.customermanagement.domain.CustomerEvent;
import io.eventuate.customerservice.customermanagement.domain.CustomerId;
import io.eventuate.customerservice.customermanagement.domain.CustomerEventPublisher;
import io.eventuate.tram.events.publisher.AbstractDomainEventPublisherForAggregateImpl;
import io.eventuate.tram.events.publisher.DomainEventPublisher;

public class CustomerEventPublisherImpl
    extends AbstractDomainEventPublisherForAggregateImpl<Customer, CustomerId, CustomerEvent>
    implements CustomerEventPublisher {

    public CustomerEventPublisherImpl(DomainEventPublisher domainEventPublisher) {
        super(Customer.class, Customer::getId, domainEventPublisher, CustomerEvent.class);
    }
}
