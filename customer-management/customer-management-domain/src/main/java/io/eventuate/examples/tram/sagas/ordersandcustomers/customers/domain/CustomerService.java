package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain;

import io.eventuate.examples.common.money.Money;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CustomerService {

  private CustomerRepository customerRepository;
  private CustomerEventPublisher customerEventPublisher;

  public CustomerService(CustomerRepository customerRepository, CustomerEventPublisher customerEventPublisher) {
    this.customerRepository = customerRepository;
    this.customerEventPublisher = customerEventPublisher;
  }

  @Transactional
  public Customer createCustomer(String name, Money creditLimit) {
    Customer customer  = new Customer(name, creditLimit);
    return customerRepository.save(customer);
  }

  public void reserveCredit(long customerId, long orderId, Money orderTotal) throws CustomerCreditLimitExceededException {
    Customer customer = customerRepository.findById(customerId).orElseThrow(CustomerNotFoundException::new);
    customer.reserveCredit(orderId, orderTotal);
    customerEventPublisher.publish(customer, new CustomerCreditReservedEvent(orderId));
  }

  public List<Customer> findAll() {
    return StreamSupport.stream(customerRepository.findAll().spliterator(), false).collect(Collectors.toList());
  }

  public Optional<Customer> findById(long customerId) {
    return customerRepository.findById(customerId);
  }
}
