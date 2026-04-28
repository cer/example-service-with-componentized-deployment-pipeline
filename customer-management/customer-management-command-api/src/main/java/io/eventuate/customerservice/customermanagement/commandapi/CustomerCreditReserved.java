package io.eventuate.customerservice.customermanagement.commandapi;

import io.eventuate.tram.commands.consumer.annotations.SuccessReply;

@SuccessReply
public record CustomerCreditReserved() implements ReserveCreditResult {
}
