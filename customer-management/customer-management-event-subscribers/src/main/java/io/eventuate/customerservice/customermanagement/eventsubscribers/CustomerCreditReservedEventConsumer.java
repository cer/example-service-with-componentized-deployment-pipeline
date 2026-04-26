package io.eventuate.customerservice.customermanagement.eventsubscribers;

import io.eventuate.customerservice.customermanagement.domain.CustomerCreditReservedEvent;
import io.eventuate.customerservice.customermanagement.domain.CustomerService;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerCreditReservedEventConsumer {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CustomerService customerService;

    public CustomerCreditReservedEventConsumer(CustomerService customerService) {
        this.customerService = customerService;
    }

    @EventuateDomainEventHandler(
        subscriberId = "customerServiceEventSubscriber",
        channel = "io.eventuate.customerservice.customermanagement.domain.Customer"
    )
    public void handleCustomerCreditReserved(DomainEventEnvelope<CustomerCreditReservedEvent> envelope) {
        CustomerCreditReservedEvent event = envelope.getEvent();
        String customerId = envelope.getAggregateId();
        logger.info("Handling CustomerCreditReserved: customerId={}, orderId={}", customerId, event.orderId());
        customerService.noteCreditReserved(customerId, event.orderId());
    }
}
