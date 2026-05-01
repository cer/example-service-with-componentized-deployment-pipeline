package io.eventuate.customerservice.customermanagement.domain;

import io.eventuate.examples.common.money.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerManagementServiceTest {

  @Mock
  private CustomerRepository customerRepository;

  @Mock
  private CustomerEventPublisher customerEventPublisher;

  @InjectMocks
  private CustomerManagementService customerManagementService;

  @Test
  void reserveCreditShouldPublishCustomerCreditReservedEvent() {
    CustomerId customerId = CustomerId.generate();
    long orderId = 101L;
    Money orderTotal = new Money("40.00");
    Customer customer = new Customer("John", new Money("100.00"));

    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

    customerManagementService.reserveCredit(customerId, orderId, orderTotal);

    assertThat(customer.availableCredit()).isEqualTo(new Money("60.00"));

    ArgumentCaptor<CustomerCreditReservedEvent> eventCaptor = ArgumentCaptor.forClass(CustomerCreditReservedEvent.class);
    verify(customerEventPublisher).publish(eq(customer), eventCaptor.capture());

    CustomerCreditReservedEvent event = eventCaptor.getValue();
    assertThat(event.orderId()).isEqualTo(orderId);
  }

  @Test
  void reserveCreditShouldThrowWhenCustomerNotFound() {
    CustomerId customerId = CustomerId.generate();

    when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customerManagementService.reserveCredit(customerId, 101L, new Money("50.00")))
        .isInstanceOf(CustomerNotFoundException.class);
    verifyNoInteractions(customerEventPublisher);
  }

  @Test
  void reserveCreditShouldNotPublishEventWhenCreditLimitExceeded() {
    CustomerId customerId = CustomerId.generate();
    long orderId = 101L;
    Money orderTotal = new Money("150.00");
    Customer customer = new Customer("John", new Money("100.00"));

    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

    assertThatThrownBy(() -> customerManagementService.reserveCredit(customerId, orderId, orderTotal))
        .isInstanceOf(CustomerCreditLimitExceededException.class);
    verifyNoInteractions(customerEventPublisher);
  }

  @Test
  void createCustomerShouldPublishCustomerCreatedEvent() {
    String name = "Jane";
    Money creditLimit = new Money("200.00");
    Customer customer = new Customer(name, creditLimit);

    when(customerRepository.save(any(Customer.class))).thenReturn(customer);

    customerManagementService.createCustomer(name, creditLimit);

    ArgumentCaptor<CustomerCreatedEvent> eventCaptor = ArgumentCaptor.forClass(CustomerCreatedEvent.class);
    verify(customerEventPublisher).publish(eq(customer), eventCaptor.capture());

    CustomerCreatedEvent event = eventCaptor.getValue();
    assertThat(event.name()).isEqualTo(name);
    assertThat(event.creditLimit()).isEqualTo(creditLimit);
  }

  @Test
  void createCustomerShouldSaveAndReturnCustomer() {
    String name = "Jane";
    Money creditLimit = new Money("200.00");
    Customer customer = new Customer(name, creditLimit);

    when(customerRepository.save(any(Customer.class))).thenReturn(customer);

    Customer result = customerManagementService.createCustomer(name, creditLimit);

    assertThat(result.getName()).isEqualTo(name);
    assertThat(result.getCreditLimit()).isEqualTo(creditLimit);
    verify(customerRepository).save(any(Customer.class));
  }

  @Test
  void findByIdShouldReturnCustomerWhenExists() {
    CustomerId customerId = CustomerId.generate();
    Customer customer = new Customer("John", new Money("100.00"));

    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

    Optional<Customer> result = customerManagementService.findCustomerById(customerId);

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("John");
  }

  @Test
  void findByIdShouldReturnEmptyWhenNotExists() {
    CustomerId customerId = CustomerId.generate();

    when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

    Optional<Customer> result = customerManagementService.findCustomerById(customerId);

    assertThat(result).isEmpty();
  }

  @Test
  void findAllShouldReturnAllCustomers() {
    Customer customer1 = new Customer("John", new Money("100.00"));
    Customer customer2 = new Customer("Jane", new Money("200.00"));

    when(customerRepository.findAll()).thenReturn(List.of(customer1, customer2));

    List<Customer> result = customerManagementService.findAllCustomers();

    assertThat(result).hasSize(2);
  }
}
