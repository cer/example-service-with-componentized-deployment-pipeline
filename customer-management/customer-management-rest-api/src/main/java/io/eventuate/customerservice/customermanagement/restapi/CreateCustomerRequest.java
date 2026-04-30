package io.eventuate.customerservice.customermanagement.restapi;

import io.eventuate.examples.common.money.Money;

public record CreateCustomerRequest(String name, Money creditLimit) {
}
