package io.eventuate.customerservice.customermanagement.restapi;
import io.eventuate.examples.common.money.Money;
import io.eventuate.customerservice.customermanagement.domain.Customer;
import io.eventuate.customerservice.customermanagement.domain.CustomerId;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.customerservice.customermanagement.sagas.CustomerManagementSagaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerManagementController.class)
@WithMockUser(roles = "USER")
public class CustomerManagementControllerTest {

  @SpringBootApplication
  static class TestApp {
  }

  // This class duplicates the security configuration in CustomerManagementWebSecurityConfiguration because that's not included

  @TestConfiguration
  @EnableWebSecurity
  @EnableMethodSecurity
  static class TestSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      return http
              .csrf(AbstractHttpConfigurer::disable)
              .build();
    }
  }

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CustomerManagementService customerManagementService;

  @MockitoBean
  private CustomerManagementSagaService customerManagementSagaService;

  @Test
  public void shouldCreateCustomer() throws Exception {
    String name = "Fred";
    Money creditLimit = new Money("15.00");
    CustomerId customerId = CustomerId.generate();

    Customer customer = new Customer(name, creditLimit);
    ReflectionTestUtils.setField(customer, "id", customerId.id());

    when(customerManagementService.createCustomer(name, creditLimit)).thenReturn(customer);

    mockMvc.perform(post("/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Fred\",\"creditLimit\":{\"amount\":15.00}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerId").value(customerId.id().toString()));
  }

  @Test
  public void shouldGetCustomers() throws Exception {
    when(customerManagementService.findAllCustomers()).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customers").isEmpty());
  }

  @Test
  public void shouldGetCustomer() throws Exception {
    CustomerId customerId = CustomerId.generate();
    String name = "Fred";
    Money creditLimit = new Money("15.00");

    Customer customer = new Customer(name, creditLimit);
    ReflectionTestUtils.setField(customer, "id", customerId.id());

    when(customerManagementService.findCustomerById(customerId)).thenReturn(Optional.of(customer));

    mockMvc.perform(get("/customers/{customerId}", customerId.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerId").value(customerId.id().toString()))
            .andExpect(jsonPath("$.name").value("Fred"))
            .andExpect(jsonPath("$.creditLimit.amount").value(15.0));
  }

  @Test
  public void shouldReturn404WhenCustomerNotFound() throws Exception {
    CustomerId customerId = CustomerId.generate();

    when(customerManagementService.findCustomerById(customerId)).thenReturn(Optional.empty());

    mockMvc.perform(get("/customers/{customerId}", customerId.id()))
            .andExpect(status().isNotFound());
  }

  @Test
  public void shouldReserveCredit() throws Exception {
    CustomerId customerId = CustomerId.generate();
    Long orderId = 99L;
    Money orderTotal = new Money("12.34");

    mockMvc.perform(post("/customers/{customerId}/creditreservations", customerId.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"orderId\":99,\"orderTotal\":{\"amount\":12.34}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PENDING"));

    verify(customerManagementSagaService).reserveCredit(customerId, orderId, orderTotal);
  }
}
