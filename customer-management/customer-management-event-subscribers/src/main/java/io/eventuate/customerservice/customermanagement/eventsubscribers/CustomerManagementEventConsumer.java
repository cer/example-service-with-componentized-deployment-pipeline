package io.eventuate.customerservice.customermanagement.eventsubscribers;

import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.otherservice.othersubdomain.domain.OtherEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerManagementEventConsumer {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CustomerManagementService customerManagementService;

    public CustomerManagementEventConsumer(CustomerManagementService customerManagementService) {
        this.customerManagementService = customerManagementService;
    }

    @EventuateDomainEventHandler(
        subscriberId = "customerServiceEventSubscriber",
        channel = "io.eventuate.otherservice.othersubdomain.domain.OtherAggregate"
    )
    public void handleOtherEvent(DomainEventEnvelope<OtherEvent> envelope) {
        OtherEvent event = envelope.getEvent();
        String aggregateId = envelope.getAggregateId();
        logger.info("Handling OtherEvent: aggregateId={}, orderId={}", aggregateId, event.orderId());
        customerManagementService.noteCreditReserved(aggregateId, event.orderId());
    }
}
