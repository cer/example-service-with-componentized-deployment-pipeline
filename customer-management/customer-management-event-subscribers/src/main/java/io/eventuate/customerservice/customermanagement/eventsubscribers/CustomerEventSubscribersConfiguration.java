package io.eventuate.customerservice.customermanagement.eventsubscribers;

import io.eventuate.customerservice.customermanagement.domain.CustomerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerEventSubscribersConfiguration {

    @Bean
    public CustomerCreditReservedEventConsumer customerCreditReservedEventConsumer(CustomerService customerService) {
        return new CustomerCreditReservedEventConsumer(customerService);
    }
}
