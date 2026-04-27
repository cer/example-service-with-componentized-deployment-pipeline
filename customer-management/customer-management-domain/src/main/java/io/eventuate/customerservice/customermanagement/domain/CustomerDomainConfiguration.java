package io.eventuate.customerservice.customermanagement.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerDomainConfiguration {

  @Bean
  public CustomerManagementService customerService(CustomerRepository customerRepository, CustomerEventPublisher customerEventPublisher) {
    return new CustomerManagementService(customerRepository, customerEventPublisher);
  }

}
