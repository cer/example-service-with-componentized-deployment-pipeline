package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.messaging;

import io.eventuate.examples.common.money.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.api.messaging.commands.ReserveCreditCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.api.messaging.replies.CustomerCreditReserved;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.api.messaging.replies.CustomerNotFound;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerNotFoundException;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerService;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.spring.inmemory.TramInMemoryConfiguration;
import io.eventuate.tram.testutil.TestMessageConsumer;
import io.eventuate.tram.testutil.TestMessageConsumerFactory;
import io.eventuate.util.test.async.Eventually;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.jdbc.AutoConfigureDataJdbc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=" // Otherwise, Error creating bean with name 'eventuateCommonJdbcOperations' ould not resolve placeholder 'spring.datasource.driver-class-name'
})
public class CustomerCommandHandlerTest {

    @Configuration
    @EnableAutoConfiguration
    @Import({CustomerCommandHandlerConfiguration.class, TramInMemoryConfiguration.class})
    static class Config {
        @Bean
        public TestMessageConsumerFactory testMessageConsumerFactory() {
            return new TestMessageConsumerFactory();
        }

    }

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private CommandProducer commandProducer;

    @Autowired
    private TestMessageConsumerFactory testMessageConsumerFactory;

    @Test
    void shouldHandleReserveCreditCommand() {
        long customerId = System.currentTimeMillis();
        long orderId = 102L;
        Money orderTotal = new Money("12.34");

        TestMessageConsumer replyConsumer = testMessageConsumerFactory.make();

        var commandId = commandProducer.send("customerService",
                new ReserveCreditCommand(customerId, orderId, orderTotal),
                replyConsumer.getReplyChannel(), Collections.emptyMap());

        replyConsumer.assertHasReplyTo(commandId);
        assertHasReplyToOfType(replyConsumer, commandId, CustomerCreditReserved.class);
        verify(customerService).reserveCredit(customerId, orderId, orderTotal);
    }

    private <T> void assertHasReplyToOfType(TestMessageConsumer replyConsumer, String commandId, Class<T> replyClass) {
        var message = replyConsumer.assertHasMessage();
        assertThat(message.getHeaders().get(ReplyMessageHeaders.IN_REPLY_TO)).isEqualTo(commandId);
        assertThat(message.getHeaders().get(ReplyMessageHeaders.REPLY_TYPE)).isEqualTo(replyClass.getName());
    }

    @Test
    void shouldReturnCustomerNotFoundWhenCustomerDoesNotExist() {
        long customerId = System.currentTimeMillis();
        long orderId = 103L;
        Money orderTotal = new Money("56.78");

        doThrow(new CustomerNotFoundException())
                .when(customerService).reserveCredit(customerId, orderId, orderTotal);

        TestMessageConsumer replyConsumer = testMessageConsumerFactory.make();

        var commandId = commandProducer.send("customerService",
                new ReserveCreditCommand(customerId, orderId, orderTotal),
                replyConsumer.getReplyChannel(), Collections.emptyMap());

        replyConsumer.assertHasReplyTo(commandId);
        assertHasReplyToOfType(replyConsumer, commandId, CustomerNotFound.class);

        verify(customerService).reserveCredit(customerId, orderId, orderTotal);
    }
}
