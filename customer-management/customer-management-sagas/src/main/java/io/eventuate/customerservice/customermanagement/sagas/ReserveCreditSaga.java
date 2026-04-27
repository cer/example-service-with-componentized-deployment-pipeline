package io.eventuate.customerservice.customermanagement.sagas;

import io.eventuate.customerservice.customermanagement.api.messaging.replies.CustomerCreditLimitExceeded;
import io.eventuate.customerservice.customermanagement.api.messaging.replies.CustomerNotFound;
import io.eventuate.customerservice.customermanagement.domain.CreditReservationDetails;
import io.eventuate.customerservice.customermanagement.domain.CustomerService;
import io.eventuate.customerservice.customermanagement.domain.RejectionReason;
import io.eventuate.customerservice.customermanagement.sagas.proxies.CustomerServiceProxy;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;

import java.util.List;

public class ReserveCreditSaga implements SimpleSaga<ReserveCreditSagaData> {

    private final CustomerService customerService;
    private final CustomerServiceProxy customerServiceProxy;

    public ReserveCreditSaga(CustomerService customerService, CustomerServiceProxy customerServiceProxy) {
        this.customerService = customerService;
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
        customerService.noteCreditReservationPending(data.toCreditReservationDetails());
    }

    private void reject(ReserveCreditSagaData data) {
        customerService.noteCreditReservationRejected(data.toCreditReservationDetails(), data.getRejectionReason());
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
        customerService.noteCreditReservationApproved(data.toCreditReservationDetails());
    }
}
