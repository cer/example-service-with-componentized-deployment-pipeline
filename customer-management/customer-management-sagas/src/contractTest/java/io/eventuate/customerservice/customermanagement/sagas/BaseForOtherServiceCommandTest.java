package io.eventuate.customerservice.customermanagement.sagas;

import io.eventuate.customerservice.customermanagement.sagas.proxies.OtherServiceProxy;
import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.spring.testing.cloudcontract.EnableEventuateTramContractVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * Base class for the tests that Spring Cloud Contract generates from a saga participant's command
 * contracts, which are unpacked from its published stubs. Each trigger method sends the command that
 * the participant's proxy builds, so the contract verifies the message customer-service actually
 * puts on the wire.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = BaseForOtherServiceCommandTest.TestConfig.class)
public abstract class BaseForOtherServiceCommandTest {

    public static final UUID CUSTOMER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final Long ORDER_ID = 102L;
    public static final Money ORDER_TOTAL = new Money("12.34");

    @Configuration
    @EnableAutoConfiguration
    @EnableEventuateTramContractVerifier
    public static class TestConfig {

        @Bean
        public OtherServiceProxy otherServiceProxy() {
            return new OtherServiceProxy();
        }
    }

    @Autowired
    private OtherServiceProxy otherServiceProxy;

    @Autowired
    private CommandProducer commandProducer;

    protected void authorizePayment() {
        send(otherServiceProxy.authorizePayment(CUSTOMER_ID, ORDER_ID, ORDER_TOTAL), "authorizePaymentReply");
    }

    private void send(CommandWithDestination command, String replyChannel) {
        commandProducer.send(command.getDestinationChannel(),
                command.getResource(),
                command.getCommand(),
                replyChannel,
                command.getExtraHeaders());
    }
}
