package io.eventuate.customerservice.customermanagement.restapi;

import io.eventuate.customerservice.customermanagement.domain.Customer;
import io.eventuate.customerservice.customermanagement.domain.CustomerId;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.customerservice.customermanagement.sagas.CustomerManagementSagaService;
import io.eventuate.examples.common.money.Money;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

/**
 * Base class for the tests that Spring Cloud Contract generates from the contracts in
 * src/contractTest/resources/contracts. It stubs the domain and saga services so that the
 * controller produces the responses that those contracts specify.
 */
@WebMvcTest(CustomerManagementController.class)
// The application class is named explicitly because the test source set puts a second
// @SpringBootApplication on the contractTest classpath, which makes the default scan ambiguous.
@ContextConfiguration(classes = ContractVerifierBase.TestApp.class)
@Import(ContractVerifierBase.TestSecurityConfig.class)
@WithMockUser(roles = "USER")
public abstract class ContractVerifierBase {

  public static final String CUSTOMER_ID = "11111111-1111-1111-1111-111111111111";
  public static final String CUSTOMER_NAME = "Fred";
  public static final Money CREDIT_LIMIT = new Money("15.00");

  @SpringBootApplication
  static class TestApp {
  }

  // @WebMvcTest's type filter drops CustomerManagementWebSecurityConfiguration from the component
  // scan, so this replaces it with a minimal chain. @EnableMethodSecurity is what makes the
  // controller's @PreAuthorize apply to the contract request.

  @Configuration
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

  @BeforeEach
  public void setup() {
    when(customerManagementService.findCustomerById(customerId(CUSTOMER_ID))).thenReturn(Optional.of(existingCustomer()));

    RestAssuredMockMvc.mockMvc(mockMvc);
  }

  @AfterEach
  public void reset() {
    RestAssuredMockMvc.reset();
  }

  private static Customer existingCustomer() {
    Customer customer = new Customer(CUSTOMER_NAME, CREDIT_LIMIT);
    ReflectionTestUtils.setField(customer, "id", UUID.fromString(CUSTOMER_ID));
    return customer;
  }

  private static CustomerId customerId(String id) {
    return new CustomerId(UUID.fromString(id));
  }
}
