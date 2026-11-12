package io.eventuate.otherservice.othersubdomain.commandapi;

import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.commands.common.Command;

import java.util.UUID;

public record AuthorizePaymentCommand(UUID customerId, Long orderId, Money orderTotal) implements Command {
}
