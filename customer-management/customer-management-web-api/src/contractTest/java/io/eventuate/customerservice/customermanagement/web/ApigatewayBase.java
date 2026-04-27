package io.eventuate.customerservice.customermanagement.web;

import io.eventuate.examples.common.money.Money;
import io.eventuate.customerservice.customermanagement.domain.Customer;
import io.eventuate.customerservice.customermanagement.domain.CustomerRepository;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ApigatewayBase {

  @BeforeEach
  public void setup() {
    CustomerManagementService customerService = mock(CustomerManagementService.class);
    CustomerRepository customerRepository = mock(CustomerRepository.class);
    CustomerManagementController orderController = new CustomerManagementController(customerService);

    Customer customer = new Customer("Chris", new Money("123.45"));
    ReflectionTestUtils.setField(customer, "id", 101L);

    when(customerService.createCustomer(customer.getName(), customer.getCreditLimit())).thenReturn(customer);
    when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

    RestAssuredMockMvc.standaloneSetup(MockMvcBuilders.standaloneSetup(orderController));

  }
}
