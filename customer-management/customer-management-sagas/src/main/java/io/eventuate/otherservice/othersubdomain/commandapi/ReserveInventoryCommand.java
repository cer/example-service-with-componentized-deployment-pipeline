package io.eventuate.otherservice.othersubdomain.commandapi;

import io.eventuate.tram.commands.common.Command;

public record ReserveInventoryCommand(Long orderId) implements Command {
}
