package io.eventuate.customerservice.customermanagement.commandapi;

import io.eventuate.customerservice.customermanagement.domain.CustomerId;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.spring.testing.cloudcontract.EnableEventuateTramContractVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static io.eventuate.util.test.async.Eventually.eventually;
import static org.mockito.Mockito.verify;

/**
 * Verifies the handler against the command contracts: each test triggers a command stub, which sends
 * the command the contract specifies, and checks that the handler handles it.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = CommandHandlersTest.TestConfig.class)
@AutoConfigureStubRunner(ids = "io.eventuate.customerservice:customer-management-command-api:+",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL)
@DirtiesContext
public class CommandHandlersTest {

  @Configuration
  @EnableAutoConfiguration
  @EnableEventuateTramContractVerifier
  @Import(CustomerCommandHandlerConfiguration.class)
  public static class TestConfig {
  }

  @Autowired
  private StubFinder stubFinder;

  @MockitoBean
  private CustomerManagementService customerManagementService;

  @Test
  public void shouldHandleReserveCreditCommand() {
    stubFinder.trigger("reserveCredit");
    eventually(() -> verify(customerManagementService).reserveCredit(
            new CustomerId(UUID.fromString("11111111-1111-1111-1111-111111111111")), 102L, new Money("12.34")));
  }
}
