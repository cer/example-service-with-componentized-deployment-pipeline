package io.eventuate.customerservice.customermanagement.commandapi;

import io.eventuate.tram.commands.consumer.annotations.FailureReply;

@FailureReply
public record CustomerNotFound() implements ReserveCreditResult {
}
