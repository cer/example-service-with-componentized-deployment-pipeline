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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

  @Mock
  private CustomerRepository customerRepository;

  @Mock
  private CustomerEventPublisher customerEventPublisher;

  @InjectMocks
  private CustomerService customerService;

  @Test
  void reserveCreditShouldPublishCustomerCreditReservedEvent() {
    long customerId = 1L;
    long orderId = 101L;
    Money orderTotal = new Money("40.00");
    Customer customer = new Customer("John", new Money("100.00"));

    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

    customerService.reserveCredit(customerId, orderId, orderTotal);

    assertEquals(new Money("60.00"), customer.availableCredit());

    ArgumentCaptor<CustomerCreditReservedEvent> eventCaptor = ArgumentCaptor.forClass(CustomerCreditReservedEvent.class);
    verify(customerEventPublisher).publish(eq(customer), eventCaptor.capture());

    CustomerCreditReservedEvent event = eventCaptor.getValue();
    assertEquals(orderId, event.orderId());
  }

  @Test
  void reserveCreditShouldThrowWhenCustomerNotFound() {
    long customerId = 1L;

    when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

    assertThrows(CustomerNotFoundException.class, () ->
        customerService.reserveCredit(customerId, 101L, new Money("50.00"))
    );
    verifyNoInteractions(customerEventPublisher);
  }

  @Test
  void reserveCreditShouldNotPublishEventWhenCreditLimitExceeded() {
    long customerId = 1L;
    long orderId = 101L;
    Money orderTotal = new Money("150.00");
    Customer customer = new Customer("John", new Money("100.00"));

    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

    assertThrows(CustomerCreditLimitExceededException.class, () ->
        customerService.reserveCredit(customerId, orderId, orderTotal)
    );
    verifyNoInteractions(customerEventPublisher);
  }

  @Test
  void createCustomerShouldSaveAndReturnCustomer() {
    String name = "Jane";
    Money creditLimit = new Money("200.00");
    Customer customer = new Customer(name, creditLimit);

    when(customerRepository.save(any(Customer.class))).thenReturn(customer);

    Customer result = customerService.createCustomer(name, creditLimit);

    assertEquals(name, result.getName());
    assertEquals(creditLimit, result.getCreditLimit());
    verify(customerRepository).save(any(Customer.class));
  }

  @Test
  void findByIdShouldReturnCustomerWhenExists() {
    long customerId = 1L;
    Customer customer = new Customer("John", new Money("100.00"));

    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

    Optional<Customer> result = customerService.findById(customerId);

    assertTrue(result.isPresent());
    assertEquals("John", result.get().getName());
  }

  @Test
  void findByIdShouldReturnEmptyWhenNotExists() {
    long customerId = 1L;

    when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

    Optional<Customer> result = customerService.findById(customerId);

    assertTrue(result.isEmpty());
  }

  @Test
  void findAllShouldReturnAllCustomers() {
    Customer customer1 = new Customer("John", new Money("100.00"));
    Customer customer2 = new Customer("Jane", new Money("200.00"));

    when(customerRepository.findAll()).thenReturn(List.of(customer1, customer2));

    List<Customer> result = customerService.findAll();

    assertEquals(2, result.size());
  }
}
