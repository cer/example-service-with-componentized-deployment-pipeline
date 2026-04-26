package io.eventuate.customerservice.customers.eventsubscribers;

import io.eventuate.customerservice.customers.domain.CustomerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerEventSubscribersConfiguration {

    @Bean
    public CustomerCreditReservedEventConsumer customerCreditReservedEventConsumer(CustomerService customerService) {
        return new CustomerCreditReservedEventConsumer(customerService);
    }
}
