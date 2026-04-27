package io.eventuate.customerservice.customermanagement.sagas.proxies;

import io.eventuate.customerservice.customermanagement.api.messaging.commands.ReserveCreditCommand;
import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerServiceProxyTest {

    @Test
    void shouldCreateReserveCreditCommand() {
        CustomerServiceProxy proxy = new CustomerServiceProxy();

        CommandWithDestination cmd = proxy.reserveCredit(101L, 102L, new Money("12.34"));

        assertThat(cmd).isNotNull();
        assertThat(cmd.getDestinationChannel()).isEqualTo("customerService");
        assertThat(cmd.getCommand()).isInstanceOf(ReserveCreditCommand.class);
        ReserveCreditCommand command = (ReserveCreditCommand) cmd.getCommand();
        assertThat(command.getCustomerId()).isEqualTo(101L);
        assertThat(command.getOrderId()).isEqualTo(102L);
        assertThat(command.getOrderTotal()).isEqualTo(new Money("12.34"));
    }
}
