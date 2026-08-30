package io.eventuate.customerservice.customermanagement.sagas;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.spring.testing.cloudcontract.EnableEventuateTramContractVerifier;
import io.eventuate.util.test.async.Eventually;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The other half of the async request/response contract: other-service's reply contract, replayed
 * against customer-service so that the reply it will actually receive is verified.
 * BaseForOtherServiceCommandTest covers the command customer-service sends.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = ReplyHandlersTest.TestConfiguration.class)
@AutoConfigureStubRunner(ids = "io.eventuate.otherservice:other-subdomain-command-api",
        stubsMode = StubRunnerProperties.StubsMode.CLASSPATH)
@DirtiesContext
public class ReplyHandlersTest {

    private static final String REPLY_CHANNEL = "reserveInventoryReply";

    @Configuration
    @EnableAutoConfiguration
    @EnableEventuateTramContractVerifier
    public static class TestConfiguration {
    }

    @Autowired
    private StubFinder stubFinder;

    @Autowired
    private MessageConsumer messageConsumer;

    private final ConcurrentLinkedQueue<Message> replies = new ConcurrentLinkedQueue<>();

    @BeforeEach
    public void subscribeToReplyChannel() {
        messageConsumer.subscribe("ReplyHandlersTest", Set.of(REPLY_CHANNEL), replies::add);
    }

    @Test
    public void shouldReceiveInventoryReservedReply() {
        stubFinder.trigger("inventoryReserved");

        Eventually.eventually(() -> {
            Message reply = replies.peek();
            assertThat(reply).isNotNull();
            assertThat(reply.getRequiredHeader("reply_type"))
                    .isEqualTo("io.eventuate.otherservice.othersubdomain.commandapi.InventoryReserved");
            assertThat(reply.getRequiredHeader("reply_outcome-type")).isEqualTo("SUCCESS");
        });
    }
}
