package io.eventuate.customerservice.customermanagement.sagas;

import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.customerservice.customermanagement.sagas.proxies.CustomerServiceProxy;
import io.eventuate.tram.sagas.orchestration.SagaInstanceFactory;
import io.eventuate.tram.spring.flyway.EnableEventuateTramFlywayMigration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableEventuateTramFlywayMigration
public class CustomerManagementSagasConfiguration {

    @Bean
    public CustomerServiceProxy customerServiceProxy() {
        return new CustomerServiceProxy();
    }

    @Bean
    public ReserveCreditSaga reserveCreditSaga(CustomerManagementService customerManagementService,
                                               CustomerServiceProxy customerServiceProxy) {
        return new ReserveCreditSaga(customerManagementService, customerServiceProxy);
    }

    @Bean
    public CustomerManagementSagaService creditReservationSagaService(SagaInstanceFactory sagaInstanceFactory,
                                                                      ReserveCreditSaga reserveCreditSaga) {
        return new CustomerManagementSagaService(sagaInstanceFactory, reserveCreditSaga);
    }
}
