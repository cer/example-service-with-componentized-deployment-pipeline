package io.eventuate.architecture.rules.fixtures.hex8.customers.api;

public record CustomerDetails(long customerId, String name, CustomerStatus status) {
}
