package io.eventuate.customerservice.customermanagement.sagas;

import io.eventuate.customerservice.customermanagement.domain.CustomerId;
import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.sagas.orchestration.SagaInstanceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CustomerManagementSagaServiceTest {

    private SagaInstanceFactory sagaInstanceFactory;
    private ReserveCreditSaga reserveCreditSaga;
    private CustomerManagementSagaService service;

    @BeforeEach
    void setUp() {
        sagaInstanceFactory = mock(SagaInstanceFactory.class);
        reserveCreditSaga = mock(ReserveCreditSaga.class);
        service = new CustomerManagementSagaService(sagaInstanceFactory, reserveCreditSaga);
    }

    @Test
    void shouldStartSagaWhenCreatingCreditReservation() {
        CustomerId customerId = CustomerId.generate();
        Long orderId = 102L;
        Money orderTotal = new Money("12.34");

        service.reserveCredit(customerId, orderId, orderTotal);

        ArgumentCaptor<ReserveCreditSagaData> dataCaptor =
                ArgumentCaptor.forClass(ReserveCreditSagaData.class);
        verify(sagaInstanceFactory).create(eq(reserveCreditSaga), dataCaptor.capture());

        ReserveCreditSagaData capturedData = dataCaptor.getValue();
        assertThat(capturedData.getCustomerId()).isEqualTo(customerId);
        assertThat(capturedData.getOrderId()).isEqualTo(orderId);
        assertThat(capturedData.getOrderTotal()).isEqualTo(orderTotal);
    }
}
