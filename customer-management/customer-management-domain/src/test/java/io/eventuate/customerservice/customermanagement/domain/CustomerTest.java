package io.eventuate.customerservice.customermanagement.domain;

import io.eventuate.examples.common.money.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CustomerTest {

  @Test
  void reserveCreditShouldReduceAvailableCredit() {
    Customer customer = new Customer("John", new Money("100.00"));

    customer.reserveCredit(1L, new Money("40.00"));

    assertEquals(new Money("60.00"), customer.availableCredit());
  }

  @Test
  void reserveCreditShouldAllowMultipleReservations() {
    Customer customer = new Customer("John", new Money("100.00"));

    customer.reserveCredit(1L, new Money("40.00"));
    customer.reserveCredit(2L, new Money("30.00"));

    assertEquals(new Money("30.00"), customer.availableCredit());
  }

  @Test
  void reserveCreditShouldThrowWhenCreditLimitExceeded() {
    Customer customer = new Customer("John", new Money("100.00"));

    assertThrows(CustomerCreditLimitExceededException.class, () ->
        customer.reserveCredit(1L, new Money("150.00"))
    );
  }

  @Test
  void reserveCreditShouldThrowWhenCumulativeReservationsExceedLimit() {
    Customer customer = new Customer("John", new Money("100.00"));
    customer.reserveCredit(1L, new Money("60.00"));

    assertThrows(CustomerCreditLimitExceededException.class, () ->
        customer.reserveCredit(2L, new Money("50.00"))
    );
  }
}
