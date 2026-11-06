package io.eventuate.customerservice.customermanagement.sagas;

import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.customerservice.customermanagement.sagas.proxies.OtherServiceProxy;
import io.eventuate.tram.sagas.orchestration.SagaInstanceFactory;
import io.eventuate.tram.spring.flyway.EnableEventuateTramFlywayMigration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableEventuateTramFlywayMigration
public class CustomerManagementSagasConfiguration {

    @Bean
    public OtherServiceProxy otherServiceProxy() {
        return new OtherServiceProxy();
    }

    @Bean
    public ReserveCreditSaga reserveCreditSaga(CustomerManagementService customerManagementService,
                                               OtherServiceProxy otherServiceProxy) {
        return new ReserveCreditSaga(customerManagementService, otherServiceProxy);
    }

    @Bean
    public CustomerManagementSagaService creditReservationSagaService(SagaInstanceFactory sagaInstanceFactory,
                                                                      ReserveCreditSaga reserveCreditSaga) {
        return new CustomerManagementSagaService(sagaInstanceFactory, reserveCreditSaga);
    }
}
