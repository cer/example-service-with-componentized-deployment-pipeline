package io.eventuate.customerservice.customermanagement.eventsubscribers;

import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerManagementEventConsumerConfiguration {

    @Bean
    public CustomerManagementEventConsumer customerCreditReservedEventConsumer(CustomerManagementService customerManagementService) {
        return new CustomerManagementEventConsumer(customerManagementService);
    }
}
