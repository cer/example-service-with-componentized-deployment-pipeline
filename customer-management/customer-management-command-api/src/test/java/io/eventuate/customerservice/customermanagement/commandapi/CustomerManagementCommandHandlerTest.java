package io.eventuate.customerservice.customermanagement.commandapi;

import io.eventuate.customerservice.customermanagement.domain.CustomerId;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.customerservice.customermanagement.domain.CustomerNotFoundException;
import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.spring.inmemory.EnableTramInMemory;
import io.eventuate.tram.spring.testing.consumer.EnableTestConsumer;
import io.eventuate.tram.testutil.TestMessageConsumer;
import io.eventuate.tram.testutil.TestMessageConsumerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=" // Otherwise, Error creating bean with name 'eventuateCommonJdbcOperations' could not resolve placeholder 'spring.datasource.driver-class-name'
})
public class CustomerManagementCommandHandlerTest {

    @Configuration
    @EnableAutoConfiguration
    @EnableTramInMemory
    @EnableTestConsumer
    @Import(CustomerCommandHandlerConfiguration.class)
    static class Config {
    }

    @MockitoBean
    private CustomerManagementService customerManagementService;

    @Autowired
    private CommandProducer commandProducer;

    @Autowired
    private TestMessageConsumerFactory testMessageConsumerFactory;

    @Test
    void shouldHandleReserveCreditCommand() {
        CustomerId customerId = CustomerId.generate();
        long orderId = 102L;
        Money orderTotal = new Money("12.34");

        TestMessageConsumer replyConsumer = testMessageConsumerFactory.make();

        var commandId = commandProducer.send("customerService",
                new ReserveCreditCommand(customerId.id(), orderId, orderTotal),
                replyConsumer.getReplyChannel(), Collections.emptyMap());

        replyConsumer.assertHasReplyTo(commandId, CustomerCreditReserved.class);
        verify(customerManagementService).reserveCredit(customerId, orderId, orderTotal);
    }

    @Test
    void shouldReturnCustomerNotFoundWhenCustomerDoesNotExist() {
        CustomerId customerId = CustomerId.generate();
        long orderId = 103L;
        Money orderTotal = new Money("56.78");

        doThrow(new CustomerNotFoundException())
                .when(customerManagementService).reserveCredit(customerId, orderId, orderTotal);

        TestMessageConsumer replyConsumer = testMessageConsumerFactory.make();

        var commandId = commandProducer.send("customerService",
                new ReserveCreditCommand(customerId.id(), orderId, orderTotal),
                replyConsumer.getReplyChannel(), Collections.emptyMap());

        replyConsumer.assertHasReplyTo(commandId, CustomerNotFound.class);
        verify(customerManagementService).reserveCredit(customerId, orderId, orderTotal);
    }
}
