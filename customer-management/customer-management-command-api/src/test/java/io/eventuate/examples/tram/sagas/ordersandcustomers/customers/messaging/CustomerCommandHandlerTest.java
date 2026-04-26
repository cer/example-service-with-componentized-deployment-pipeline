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
import io.eventuate.tram.sagas.spring.inmemory.TramSagaInMemoryConfiguration;
import io.eventuate.util.test.async.Eventually;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.jdbc.AutoConfigureDataJdbc;
import org.springframework.boot.test.context.SpringBootTest;
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
@AutoConfigureDataJdbc
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
public class CustomerCommandHandlerTest {

    @Configuration
    @EnableAutoConfiguration
    @Import({CustomerCommandHandlerConfiguration.class, TramSagaInMemoryConfiguration.class})
    static class Config {
    }

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private CommandProducer commandProducer;

    @Autowired
    private MessageConsumer messageConsumer;

    @Test
    void shouldHandleReserveCreditCommand() {
        long customerId = System.currentTimeMillis();
        long orderId = 102L;
        Money orderTotal = new Money("12.34");
        String replyTo = "reply-channel-" + System.currentTimeMillis();

        List<Message> replies = new CopyOnWriteArrayList<>();
        messageConsumer.subscribe("test-success-" + System.currentTimeMillis(),
                Collections.singleton(replyTo), replies::add);

        commandProducer.send("customerService",
                new ReserveCreditCommand(customerId, orderId, orderTotal),
                replyTo, Collections.emptyMap());

        Eventually.eventually(() -> {
            verify(customerService).reserveCredit(customerId, orderId, orderTotal);
            assertThat(replies).hasSize(1);
            assertThat(replies.get(0).getRequiredHeader(ReplyMessageHeaders.REPLY_TYPE))
                    .isEqualTo(CustomerCreditReserved.class.getName());
        });
    }

    @Test
    void shouldReturnCustomerNotFoundWhenCustomerDoesNotExist() {
        long customerId = System.currentTimeMillis();
        long orderId = 103L;
        Money orderTotal = new Money("56.78");
        String replyTo = "reply-channel-" + System.currentTimeMillis();

        doThrow(new CustomerNotFoundException())
                .when(customerService).reserveCredit(customerId, orderId, orderTotal);

        List<Message> replies = new CopyOnWriteArrayList<>();
        messageConsumer.subscribe("test-failure-" + System.currentTimeMillis(),
                Collections.singleton(replyTo), replies::add);

        commandProducer.send("customerService",
                new ReserveCreditCommand(customerId, orderId, orderTotal),
                replyTo, Collections.emptyMap());

        Eventually.eventually(() -> {
            verify(customerService).reserveCredit(customerId, orderId, orderTotal);
            assertThat(replies).hasSize(1);
            assertThat(replies.get(0).getRequiredHeader(ReplyMessageHeaders.REPLY_TYPE))
                    .isEqualTo(CustomerNotFound.class.getName());
        });
    }
}
