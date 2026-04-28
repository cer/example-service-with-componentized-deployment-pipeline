package io.eventuate.customerservice.customermanagement.commandapi;

import io.eventuate.tram.spring.flyway.EnableEventuateTramFlywayMigration;
import io.eventuate.tram.spring.optimisticlocking.OptimisticLockingDecoratorConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableEventuateTramFlywayMigration
@Import({OptimisticLockingDecoratorConfiguration.class,
        CustomerCommandHandlerConfiguration.class})
public class CustomerManagementCommandHandlerWithFlywayConfiguration {



}
