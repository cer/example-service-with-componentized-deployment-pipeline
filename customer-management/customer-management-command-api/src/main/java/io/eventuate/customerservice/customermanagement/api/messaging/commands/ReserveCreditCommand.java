package io.eventuate.customerservice.customermanagement.api.messaging.commands;

import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.commands.common.Command;

import java.util.UUID;

public record ReserveCreditCommand(UUID customerId, Long orderId, Money orderTotal) implements Command {
}
