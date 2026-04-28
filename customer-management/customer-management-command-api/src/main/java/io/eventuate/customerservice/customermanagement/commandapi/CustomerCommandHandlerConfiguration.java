package io.eventuate.customerservice.customermanagement.commandapi;

import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerCommandHandlerConfiguration {

  @Bean
  public CustomerManagementCommandHandler customerCommandHandler(CustomerManagementService customerManagementService) {
    return new CustomerManagementCommandHandler(customerManagementService);
  }


}
