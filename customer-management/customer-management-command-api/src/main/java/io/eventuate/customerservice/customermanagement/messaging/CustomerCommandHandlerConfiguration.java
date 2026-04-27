package io.eventuate.customerservice.customermanagement.messaging;

import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerCommandHandlerConfiguration {

  @Bean
  public CustomerCommandHandler customerCommandHandler(CustomerManagementService customerManagementService) {
    return new CustomerCommandHandler(customerManagementService);
  }


}
