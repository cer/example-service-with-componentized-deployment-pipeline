package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.eventsubscribers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerEventSubscribersConfiguration {

    @Bean
    public CustomerCreditReservedEventConsumer customerCreditReservedEventConsumer() {
        return new CustomerCreditReservedEventConsumer();
    }
}
