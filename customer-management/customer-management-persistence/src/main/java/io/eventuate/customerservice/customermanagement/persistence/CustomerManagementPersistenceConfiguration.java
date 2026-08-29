package io.eventuate.customerservice.customermanagement.persistence;

import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackageClasses = {CustomerManagementService.class})
@EntityScan(basePackageClasses = {CustomerManagementService.class})
public class CustomerManagementPersistenceConfiguration {



}
