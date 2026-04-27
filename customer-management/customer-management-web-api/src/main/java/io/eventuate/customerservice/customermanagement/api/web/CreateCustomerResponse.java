package io.eventuate.customerservice.customermanagement.api.web;


public class CreateCustomerResponse {
  private String customerId;

  public CreateCustomerResponse() {
  }

  public CreateCustomerResponse(String customerId) {
    this.customerId = customerId;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }
}
