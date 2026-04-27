package io.eventuate.customerservice.customermanagement.sagas;

import io.eventuate.customerservice.customermanagement.domain.CreditReservationDetails;
import io.eventuate.customerservice.customermanagement.domain.RejectionReason;
import io.eventuate.examples.common.money.Money;

public class ReserveCreditSagaData {

    private Long customerId;
    private Long orderId;
    private Money orderTotal;
    private Long creditReservationId;
    private RejectionReason rejectionReason;

    public ReserveCreditSagaData() {
    }

    public ReserveCreditSagaData(Long customerId, Long orderId, Money orderTotal) {
        this.customerId = customerId;
        this.orderId = orderId;
        this.orderTotal = orderTotal;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Money getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(Money orderTotal) {
        this.orderTotal = orderTotal;
    }

    public Long getCreditReservationId() {
        return creditReservationId;
    }

    public void setCreditReservationId(Long creditReservationId) {
        this.creditReservationId = creditReservationId;
    }

    public RejectionReason getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(RejectionReason rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public CreditReservationDetails toCreditReservationDetails() {
        return new CreditReservationDetails(customerId, orderId, orderTotal);
    }
}
