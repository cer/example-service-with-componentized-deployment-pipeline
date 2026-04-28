package io.eventuate.customerservice.customermanagement.restapi;

import io.eventuate.examples.common.money.Money;

public class ReserveCreditRequest {

    private Long orderId;
    private Money orderTotal;

    public ReserveCreditRequest() {
    }

    public ReserveCreditRequest(Long orderId, Money orderTotal) {
        this.orderId = orderId;
        this.orderTotal = orderTotal;
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
}
