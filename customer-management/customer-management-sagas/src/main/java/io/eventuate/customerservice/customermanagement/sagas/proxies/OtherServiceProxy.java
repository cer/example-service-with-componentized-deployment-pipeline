package io.eventuate.customerservice.customermanagement.sagas.proxies;

import io.eventuate.examples.common.money.Money;
import io.eventuate.otherservice.othersubdomain.commandapi.PaymentDeclined;
import io.eventuate.otherservice.othersubdomain.commandapi.AuthorizePaymentCommand;
import io.eventuate.otherservice.othersubdomain.commandapi.AuthorizePaymentResult;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.commands.consumer.CommandWithDestinationBuilder;
import io.eventuate.tram.sagas.simpledsl.annotations.SagaParticipantOperation;
import io.eventuate.tram.sagas.simpledsl.annotations.SagaParticipantProxy;

import java.util.UUID;

@SagaParticipantProxy(channel = OtherServiceProxy.CHANNEL)
public class OtherServiceProxy {

    public static final String CHANNEL = "otherService";

    public static final Class<PaymentDeclined> paymentDeclinedReply = PaymentDeclined.class;

    @SagaParticipantOperation(commandClass = AuthorizePaymentCommand.class, replyClasses = AuthorizePaymentResult.class)
    public CommandWithDestination authorizePayment(UUID customerId, Long orderId, Money orderTotal) {
        return CommandWithDestinationBuilder.send(new AuthorizePaymentCommand(customerId, orderId, orderTotal))
                .to(CHANNEL)
                .build();
    }
}
