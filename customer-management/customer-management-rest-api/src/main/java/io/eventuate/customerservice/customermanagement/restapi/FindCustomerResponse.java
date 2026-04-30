package io.eventuate.customerservice.customermanagement.restapi;

import io.eventuate.examples.common.money.Money;

public record FindCustomerResponse(String customerId, String name, Money creditLimit) {
}
