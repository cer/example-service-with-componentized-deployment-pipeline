package io.eventuate.customerservice.customermanagement.domain;

import io.eventuate.examples.common.money.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CustomerTest {

  @Test
  void reserveCreditShouldReduceAvailableCredit() {
    Customer customer = new Customer("John", new Money("100.00"));

    customer.reserveCredit(1L, new Money("40.00"));

    assertThat(customer.availableCredit()).isEqualTo(new Money("60.00"));
  }

  @Test
  void reserveCreditShouldAllowMultipleReservations() {
    Customer customer = new Customer("John", new Money("100.00"));

    customer.reserveCredit(1L, new Money("40.00"));
    customer.reserveCredit(2L, new Money("30.00"));

    assertThat(customer.availableCredit()).isEqualTo(new Money("30.00"));
  }

  @Test
  void reserveCreditShouldThrowWhenCreditLimitExceeded() {
    Customer customer = new Customer("John", new Money("100.00"));

    assertThatThrownBy(() -> customer.reserveCredit(1L, new Money("150.00")))
        .isInstanceOf(CustomerCreditLimitExceededException.class);
  }

  @Test
  void reserveCreditShouldThrowWhenCumulativeReservationsExceedLimit() {
    Customer customer = new Customer("John", new Money("100.00"));
    customer.reserveCredit(1L, new Money("60.00"));

    assertThatThrownBy(() -> customer.reserveCredit(2L, new Money("50.00")))
        .isInstanceOf(CustomerCreditLimitExceededException.class);
  }
}
