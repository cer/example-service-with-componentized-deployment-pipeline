package io.eventuate.customerservice.customermanagement.sagas.proxies;

import io.eventuate.customerservice.customermanagement.commandapi.ReserveCreditCommand;
import io.eventuate.customerservice.customermanagement.commandapi.CustomerCreditLimitExceeded;
import io.eventuate.customerservice.customermanagement.commandapi.CustomerNotFound;
import io.eventuate.customerservice.customermanagement.commandapi.ReserveCreditResult;
import io.eventuate.customerservice.customermanagement.domain.CustomerId;
import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.commands.consumer.CommandWithDestinationBuilder;
import io.eventuate.tram.sagas.simpledsl.annotations.SagaParticipantOperation;
import io.eventuate.tram.sagas.simpledsl.annotations.SagaParticipantProxy;

@SagaParticipantProxy(channel = CustomerServiceProxy.CHANNEL)
public class CustomerServiceProxy {

    public static final String CHANNEL = "customerService";

    public static final Class<CustomerNotFound> customerNotFoundReply = CustomerNotFound.class;
    public static final Class<CustomerCreditLimitExceeded> creditLimitExceededReply = CustomerCreditLimitExceeded.class;

    @SagaParticipantOperation(commandClass = ReserveCreditCommand.class, replyClasses = ReserveCreditResult.class)
    public CommandWithDestination reserveCredit(CustomerId customerId, Long orderId, Money orderTotal) {
        return CommandWithDestinationBuilder.send(new ReserveCreditCommand(customerId.id(), orderId, orderTotal))
                .to(CHANNEL)
                .build();
    }
}
