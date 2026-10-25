package io.eventuate.architecture.rules.fixtures.hex8.customers.api;

public interface CustomerService {

    CustomerDetails findCustomer(long customerId);
}
