package io.eventuate.architecture.rules.fixtures.sub4.orders.eventsubscribers;

import io.eventuate.architecture.rules.fixtures.sub4.customers.domain.CustomerCreditReserved;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;

public class OrderEventConsumer {

    @EventuateDomainEventHandler(subscriberId = "orderEventConsumer",
            channel = "io.eventuate.architecture.rules.fixtures.sub4.customers.domain.Customer")
    public void handleCustomerCreditReserved(DomainEventEnvelope<CustomerCreditReserved> envelope) {
    }
}
