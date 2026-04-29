package io.eventuate.customerservice.customermanagement.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerManagementDomainConfiguration {

  @Bean
  public CustomerManagementService customerManagementService(CustomerRepository customerRepository, CustomerEventPublisher customerEventPublisher) {
    return new CustomerManagementService(customerRepository, customerEventPublisher);
  }

}
