package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.eventsubscribers;

import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerCreditReservedEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerCreditReservedEventConsumer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @EventuateDomainEventHandler(
        subscriberId = "customerServiceEventSubscriber",
        channel = "io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer"
    )
    public void handleCustomerCreditReserved(DomainEventEnvelope<CustomerCreditReservedEvent> envelope) {
        CustomerCreditReservedEvent event = envelope.getEvent();
        String customerId = envelope.getAggregateId();
        logger.info("Handling CustomerCreditReserved: customerId={}, orderId={}", customerId, event.orderId());
    }
}
