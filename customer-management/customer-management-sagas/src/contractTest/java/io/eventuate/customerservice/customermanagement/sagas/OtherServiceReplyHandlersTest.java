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
 * The other half of the async request/response contract: a saga participant's reply contracts,
 * replayed from its stubs in mavenLocal against customer-service, so that the replies it will
 * actually receive are verified. The participant's command contract tests cover the commands
 * customer-service sends.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = OtherServiceReplyHandlersTest.TestConfiguration.class)
@AutoConfigureStubRunner(ids = "io.eventuate.otherservice:other-subdomain-commands:+",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL)
@DirtiesContext
public class OtherServiceReplyHandlersTest {

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
        messageConsumer.subscribe("OtherServiceReplyHandlersTest", Set.of("authorizePaymentReply"), replies::add);
    }

    @Test
    public void shouldReceiveAuthorizePaymentSuccessReply() {
        stubFinder.trigger("authorizePaymentSuccessReply");
        assertReplyReceived("io.eventuate.otherservice.othersubdomain.commandapi.PaymentAuthorized", "SUCCESS");
    }

    @Test
    public void shouldReceiveAuthorizePaymentFailureReply() {
        stubFinder.trigger("authorizePaymentFailureReply");
        assertReplyReceived("io.eventuate.otherservice.othersubdomain.commandapi.PaymentDeclined", "FAILURE");
    }

    private void assertReplyReceived(String replyType, String outcome) {
        Eventually.eventually(() ->
            assertThat(replies).anySatisfy(reply -> {
                assertThat(reply.getRequiredHeader("reply_type")).isEqualTo(replyType);
                assertThat(reply.getRequiredHeader("reply_outcome-type")).isEqualTo(outcome);
            }));
    }
}
