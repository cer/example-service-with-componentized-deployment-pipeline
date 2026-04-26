package io.eventuate.customerservice.customers.web;

import io.eventuate.common.testcontainers.PropertyProvidingContainer;
import io.eventuate.examples.springauthorizationserver.testcontainers.AuthorizationServerContainerForLocalTests;
import io.eventuate.customerservice.customers.domain.CustomerService;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.oauth2;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes=CustomerWebIntegrationTest.Config.class)
public class CustomerWebIntegrationTest {

  @LocalServerPort
  private int port;

  public static AuthorizationServerContainerForLocalTests authorizationServer = new AuthorizationServerContainerForLocalTests()
          .withUserDb()
          .withEnv("USERS_INITIAL_0_USERNAME", "user")
          .withEnv("USERS_INITIAL_0_PASSWORD", "password")
          .withEnv("USERS_INITIAL_0_ROLES_0_", "USER")
          .withEnv("USERS_INITIAL_0_ENABLED", "true")
          .withReuse(true);


  @DynamicPropertySource
  static void startAndProvideProperties(DynamicPropertyRegistry registry) {
    PropertyProvidingContainer.startAndProvideProperties(registry, authorizationServer);
  }

  @Configuration
  @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
  @Import({CustomerWebConfiguration.class})
  public static class Config {
  }

  @MockitoBean
  private CustomerService customerService;

  @BeforeEach
  public void setup() {
    RestAssured.port = port;
    RestAssured.authentication = oauth2(authorizationServer.getJwt());
  }

  @Test
  public void shouldGetCustomers() {
    given().when()
            .log().all()
            .get("/customers")
            .then()
            .statusCode(200);
  }

}
