package io.eventuate.customerservice.customermanagement.sagas;

import io.eventuate.customerservice.customermanagement.domain.CustomerService;
import io.eventuate.customerservice.customermanagement.sagas.proxies.CustomerServiceProxy;
import io.eventuate.tram.sagas.orchestration.SagaInstanceFactory;
import io.eventuate.tram.spring.flyway.EventuateTramFlywayMigrationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateTramFlywayMigrationConfiguration.class)
public class CustomerManagementSagasConfiguration {

    @Bean
    public CustomerServiceProxy customerServiceProxy() {
        return new CustomerServiceProxy();
    }

    @Bean
    public ReserveCreditSaga reserveCreditSaga(CustomerService customerService,
                                                                    CustomerServiceProxy customerServiceProxy) {
        return new ReserveCreditSaga(customerService, customerServiceProxy);
    }

    @Bean
    public CustomerManagementSagaService creditReservationSagaService(SagaInstanceFactory sagaInstanceFactory,
                                                                      ReserveCreditSaga reserveCreditSaga) {
        return new CustomerManagementSagaService(sagaInstanceFactory, reserveCreditSaga);
    }
}
