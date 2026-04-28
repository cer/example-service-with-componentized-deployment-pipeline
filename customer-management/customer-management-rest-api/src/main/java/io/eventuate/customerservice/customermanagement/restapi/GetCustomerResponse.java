package io.eventuate.customerservice.customermanagement.restapi;


import io.eventuate.examples.common.money.Money;

public class GetCustomerResponse {
  private String customerId;
  private String name;
  private Money creditLimit;

  public GetCustomerResponse() {
  }

  public GetCustomerResponse(String customerId, String name, Money creditLimit) {
    this.customerId = customerId;
    this.name = name;
    this.creditLimit = creditLimit;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Money getCreditLimit() {
    return creditLimit;
  }

  public void setCreditLimit(Money creditLimit) {
    this.creditLimit = creditLimit;
  }
}
