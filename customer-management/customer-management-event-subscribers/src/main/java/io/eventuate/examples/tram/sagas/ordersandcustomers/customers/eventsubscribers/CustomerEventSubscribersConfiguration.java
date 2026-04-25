package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.eventsubscribers;

import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerEventSubscribersConfiguration {

    @Bean
    public CustomerCreditReservedEventConsumer customerCreditReservedEventConsumer(CustomerService customerService) {
        return new CustomerCreditReservedEventConsumer(customerService);
    }
}
