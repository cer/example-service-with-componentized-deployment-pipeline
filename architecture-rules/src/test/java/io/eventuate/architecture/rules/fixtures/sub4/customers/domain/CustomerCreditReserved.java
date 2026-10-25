package io.eventuate.architecture.rules.fixtures.sub4.customers.domain;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;

public record CustomerCreditReserved(String orderId) implements DomainEvent {

    @EventuateDomainEventHandler(subscriberId = "copiedSubscriber",
            channel = "io.eventuate.architecture.rules.fixtures.sub4.customers.domain.Customer")
    public void handle(DomainEventEnvelope<CustomerCreditReserved> envelope) {
    }
}
