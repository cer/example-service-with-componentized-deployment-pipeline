package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain;

import io.eventuate.examples.common.money.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    Money orderTotal = new Money("50.00");
    Customer customer = new Customer("John", new Money("100.00"));

    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

    customerService.reserveCredit(customerId, orderId, orderTotal);

    ArgumentCaptor<CustomerCreditReservedEvent> eventCaptor = ArgumentCaptor.forClass(CustomerCreditReservedEvent.class);
    verify(customerEventPublisher).publish(eq(customer), eventCaptor.capture());

    CustomerCreditReservedEvent event = eventCaptor.getValue();
    assertEquals(orderId, event.orderId());
  }
}
