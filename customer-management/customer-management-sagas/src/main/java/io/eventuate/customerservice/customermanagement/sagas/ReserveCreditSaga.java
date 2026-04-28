package io.eventuate.customerservice.customermanagement.sagas;

import io.eventuate.customerservice.customermanagement.commandapi.CustomerCreditLimitExceeded;
import io.eventuate.customerservice.customermanagement.commandapi.CustomerNotFound;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.customerservice.customermanagement.domain.RejectionReason;
import io.eventuate.customerservice.customermanagement.sagas.proxies.CustomerServiceProxy;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;

import java.util.List;

public class ReserveCreditSaga implements SimpleSaga<ReserveCreditSagaData> {

    private final CustomerManagementService customerManagementService;
    private final CustomerServiceProxy customerServiceProxy;

    public ReserveCreditSaga(CustomerManagementService customerManagementService, CustomerServiceProxy customerServiceProxy) {
        this.customerManagementService = customerManagementService;
        this.customerServiceProxy = customerServiceProxy;
    }

    @Override
    public List<Object> getParticipantProxies() {
        return List.of(customerServiceProxy);
    }

    private final SagaDefinition<ReserveCreditSagaData> sagaDefinition =
            step()
                .invokeLocal(this::create)
                .withCompensation(this::reject)
            .step()
                .invokeParticipant(this::reserveCredit)
                .onReply(CustomerServiceProxy.customerNotFoundReply, this::handleCustomerNotFound)
                .onReply(CustomerServiceProxy.creditLimitExceededReply, this::handleCreditLimitExceeded)
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

    private CommandWithDestination reserveCredit(ReserveCreditSagaData data) {
        return customerServiceProxy.reserveCredit(data.getCustomerId(), data.getOrderId(), data.getOrderTotal());
    }

    private void handleCustomerNotFound(ReserveCreditSagaData data, CustomerNotFound reply) {
        data.setRejectionReason(RejectionReason.UNKNOWN_CUSTOMER);
    }

    private void handleCreditLimitExceeded(ReserveCreditSagaData data, CustomerCreditLimitExceeded reply) {
        data.setRejectionReason(RejectionReason.INSUFFICIENT_CREDIT);
    }

    private void approve(ReserveCreditSagaData data) {
        customerManagementService.noteCreditReservationApproved(data.toCreditReservationDetails());
    }
}
