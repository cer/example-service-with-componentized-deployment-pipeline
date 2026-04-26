package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.web;


import io.eventuate.examples.common.money.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerControllerTest {

  @Mock
  private CustomerService customerService;

  @InjectMocks
  private CustomerController customerController;

  @Test
  public void shouldCreateCustomer() {
    String name = "Fred";
    Money creditLimit = new Money("15.00");
    Long customerId = 42L;

    Customer customer = new Customer(name, creditLimit);
    ReflectionTestUtils.setField(customer, "id", customerId);

    when(customerService.createCustomer(name, creditLimit)).thenReturn(customer);

    given()
            .standaloneSetup(customerController)
            .contentType(JSON)
            .body(new io.eventuate.examples.tram.sagas.ordersandcustomers.customers.api.web.CreateCustomerRequest(name, creditLimit))
      .when()
            .post("/customers")
            .then()
            .log().ifValidationFails()
            .statusCode(HttpStatus.OK.value())
            .contentType(JSON)
            .body("customerId", equalTo(customerId.intValue()));
  }

  @Test
  public void shouldGetCustomers() {
    when(customerService.findAll()).thenReturn(Collections.emptyList());

    given()
            .standaloneSetup(customerController)
      .when()
            .get("/customers")
            .then()
            .log().ifValidationFails()
            .statusCode(HttpStatus.OK.value())
            .contentType(JSON)
            .and().body("customers", empty());
  }
}