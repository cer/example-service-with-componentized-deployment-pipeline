package io.eventuate.customerservice.customermanagement.sagas;

import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.customerservice.customermanagement.domain.RejectionReason;
import io.eventuate.customerservice.customermanagement.sagas.proxies.OtherServiceProxy;
import io.eventuate.otherservice.othersubdomain.commandapi.InventoryOutOfStock;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;

import java.util.List;

public class ReserveCreditSaga implements SimpleSaga<ReserveCreditSagaData> {

    private final CustomerManagementService customerManagementService;
    private final OtherServiceProxy otherServiceProxy;

    public ReserveCreditSaga(CustomerManagementService customerManagementService,
                             OtherServiceProxy otherServiceProxy) {
        this.customerManagementService = customerManagementService;
        this.otherServiceProxy = otherServiceProxy;
    }

    @Override
    public List<Object> getParticipantProxies() {
        return List.of(otherServiceProxy);
    }

    private final SagaDefinition<ReserveCreditSagaData> sagaDefinition =
            step()
                .invokeLocal(this::create)
                .withCompensation(this::reject)
            .step()
                .invokeParticipant(this::reserveInventory)
                .onReply(OtherServiceProxy.inventoryOutOfStockReply, this::handleInventoryOutOfStock)
            .step()
                .invokeLocal(this::approve)
            .build();

    @Override
    public SagaDefinition<ReserveCreditSagaData> getSagaDefinition() {
        return sagaDefinition;
    }

    private void create(ReserveCreditSagaData data) {
        customerManagementService.noteCreditReservationPending(data.toCreditReservationDetails());
    }

    private void reject(ReserveCreditSagaData data) {
        customerManagementService.noteCreditReservationRejected(data.toCreditReservationDetails(), data.getRejectionReason());
    }

    private CommandWithDestination reserveInventory(ReserveCreditSagaData data) {
        return otherServiceProxy.reserveInventory(data.getOrderId());
    }

    private void handleInventoryOutOfStock(ReserveCreditSagaData data, InventoryOutOfStock reply) {
        data.setRejectionReason(RejectionReason.OUT_OF_STOCK);
    }

    private void approve(ReserveCreditSagaData data) {
        customerManagementService.noteCreditReservationApproved(data.toCreditReservationDetails());
    }
}
