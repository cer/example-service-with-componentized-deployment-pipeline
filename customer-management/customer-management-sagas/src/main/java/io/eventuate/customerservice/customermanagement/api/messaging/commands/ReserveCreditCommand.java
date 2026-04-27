package io.eventuate.customerservice.customermanagement.api.messaging.commands;

import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.commands.common.Command;

import java.util.UUID;

public class ReserveCreditCommand implements Command {
  private Long orderId;
  private Money orderTotal;
  private UUID customerId;

  public ReserveCreditCommand() {
  }

  public ReserveCreditCommand(UUID customerId, Long orderId, Money orderTotal) {
    this.customerId = customerId;
    this.orderId = orderId;
    this.orderTotal = orderTotal;
  }

  public Money getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(Money orderTotal) {
    this.orderTotal = orderTotal;
  }

  public Long getOrderId() {

    return orderId;
  }

  public void setOrderId(Long orderId) {

    this.orderId = orderId;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
  }
}
