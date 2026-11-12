package io.eventuate.customerservice.customermanagement.commandapi;

import io.eventuate.customerservice.customermanagement.domain.CustomerId;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.customerservice.customermanagement.domain.CustomerNotFoundException;
import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.spring.testing.cloudcontract.EnableEventuateTramContractVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;

/**
 * Base class for the tests that Spring Cloud Contract generates from the reply contracts in
 * src/contractTest/resources/contracts. Each trigger method sends the command to the handler, which
 * sends the reply that the contract specifies. The command contracts are verified by the
 * orchestrators that send them, so build.gradle leaves them out of the generated tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = ContractVerifierBase.TestConfig.class)
public abstract class ContractVerifierBase {

  @Configuration
  @EnableAutoConfiguration
  @EnableEventuateTramContractVerifier
  @Import(CustomerCommandHandlerConfiguration.class)
  public static class TestConfig {
  }

  private static final UUID CUSTOMER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final long ORDER_ID = 102L;
  private static final Money ORDER_TOTAL = new Money("12.34");

  @MockitoBean
  private CustomerManagementService customerManagementService;

  @Autowired
  private CommandProducer commandProducer;

  protected void reserveCredit() {
    // Called only by the test generated from the command contract, which ignoredFiles disables
    throw new UnsupportedOperationException();
  }

  protected void reserveCreditSuccessReply() {
    sendReserveCreditCommand();
  }

  protected void reserveCreditFailureReply() {
    doThrow(new CustomerNotFoundException()).when(customerManagementService)
            .reserveCredit(new CustomerId(CUSTOMER_ID), ORDER_ID, ORDER_TOTAL);
    sendReserveCreditCommand();
  }

  private void sendReserveCreditCommand() {
    commandProducer.send("customerService", new ReserveCreditCommand(CUSTOMER_ID, ORDER_ID, ORDER_TOTAL),
            "reserveCreditReply", Collections.emptyMap());
  }
}
