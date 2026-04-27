package io.eventuate.customerservice.customermanagement.domain;

import io.eventuate.examples.common.money.Money;

public record CreditReservationDetails(CustomerId customerId, Long orderId, Money orderTotal) {
}
