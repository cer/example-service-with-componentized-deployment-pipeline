package io.eventuate.customerservice.customermanagement.sagas;

import io.eventuate.customerservice.customermanagement.domain.CustomerId;
import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.sagas.orchestration.SagaInstanceFactory;
import org.springframework.transaction.annotation.Transactional;

public class CustomerManagementSagaService {

    private final SagaInstanceFactory sagaInstanceFactory;
    private final ReserveCreditSaga reserveCreditSaga;

    public CustomerManagementSagaService(SagaInstanceFactory sagaInstanceFactory,
                                         ReserveCreditSaga reserveCreditSaga) {
        this.sagaInstanceFactory = sagaInstanceFactory;
        this.reserveCreditSaga = reserveCreditSaga;
    }

    @Transactional
    public void reserveCredit(CustomerId customerId, Long orderId, Money orderTotal) {
        ReserveCreditSagaData data = new ReserveCreditSagaData(customerId, orderId, orderTotal);
        sagaInstanceFactory.create(reserveCreditSaga, data);
    }
}
