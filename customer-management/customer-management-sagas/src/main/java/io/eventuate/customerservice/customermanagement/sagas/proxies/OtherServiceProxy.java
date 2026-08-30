package io.eventuate.customerservice.customermanagement.sagas.proxies;

import io.eventuate.otherservice.othersubdomain.commandapi.InventoryOutOfStock;
import io.eventuate.otherservice.othersubdomain.commandapi.ReserveInventoryCommand;
import io.eventuate.otherservice.othersubdomain.commandapi.ReserveInventoryResult;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.commands.consumer.CommandWithDestinationBuilder;
import io.eventuate.tram.sagas.simpledsl.annotations.SagaParticipantOperation;
import io.eventuate.tram.sagas.simpledsl.annotations.SagaParticipantProxy;

@SagaParticipantProxy(channel = OtherServiceProxy.CHANNEL)
public class OtherServiceProxy {

    public static final String CHANNEL = "otherService";

    public static final Class<InventoryOutOfStock> inventoryOutOfStockReply = InventoryOutOfStock.class;

    @SagaParticipantOperation(commandClass = ReserveInventoryCommand.class, replyClasses = ReserveInventoryResult.class)
    public CommandWithDestination reserveInventory(Long orderId) {
        return CommandWithDestinationBuilder.send(new ReserveInventoryCommand(orderId))
                .to(CHANNEL)
                .build();
    }
}
