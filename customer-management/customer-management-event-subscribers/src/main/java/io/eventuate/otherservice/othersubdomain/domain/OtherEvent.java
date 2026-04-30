package io.eventuate.otherservice.othersubdomain.domain;

import io.eventuate.tram.events.common.DomainEvent;

public record OtherEvent(Long orderId) implements DomainEvent {
}
