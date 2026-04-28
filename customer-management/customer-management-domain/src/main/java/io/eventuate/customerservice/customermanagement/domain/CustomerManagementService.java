package io.eventuate.customerservice.customermanagement.domain;

import io.eventuate.examples.common.money.Money;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CustomerManagementService {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final CustomerRepository customerRepository;
  private final CustomerEventPublisher customerEventPublisher;

  public CustomerManagementService(CustomerRepository customerRepository, CustomerEventPublisher customerEventPublisher) {
    this.customerRepository = customerRepository;
    this.customerEventPublisher = customerEventPublisher;
  }

  @Transactional
  public Customer createCustomer(String name, Money creditLimit) {
    Customer customer  = new Customer(name, creditLimit);
    Customer savedCustomer = customerRepository.save(customer);
    customerEventPublisher.publish(savedCustomer, new CustomerCreatedEvent(name, creditLimit));
    return savedCustomer;
  }

  public void reserveCredit(CustomerId customerId, long orderId, Money orderTotal) throws CustomerCreditLimitExceededException {
    Customer customer = customerRepository.findById(customerId).orElseThrow(CustomerNotFoundException::new);
    customer.reserveCredit(orderId, orderTotal);
    customerEventPublisher.publish(customer, new CustomerCreditReservedEvent(orderId));
  }

  public List<Customer> findAll() {
    return StreamSupport.stream(customerRepository.findAll().spliterator(), false).collect(Collectors.toList());
  }

  public Optional<Customer> findById(CustomerId customerId) {
    return customerRepository.findById(customerId);
  }

  public void noteCreditReserved(String customerId, Long orderId) {
    logger.info("noteCreditReserved: customerId={}, orderId={}", customerId, orderId);
  }

  public void noteCreditReservationPending(CreditReservationDetails details) {
    logger.info("noteCreditReservationPending: customerId={}, orderId={}, orderTotal={}", details.customerId(), details.orderId(), details.orderTotal());
  }

  public void noteCreditReservationApproved(CreditReservationDetails details) {
    logger.info("noteCreditReservationApproved: customerId={}, orderId={}", details.customerId(), details.orderId());
  }

  public void noteCreditReservationRejected(CreditReservationDetails details, RejectionReason reason) {
    logger.info("noteCreditReservationRejected: customerId={}, orderId={}, reason={}", details.customerId(), details.orderId(), reason);
  }
}
