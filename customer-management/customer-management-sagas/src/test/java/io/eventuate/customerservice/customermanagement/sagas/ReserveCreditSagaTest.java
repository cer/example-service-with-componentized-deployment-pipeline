package io.eventuate.customerservice.customermanagement.sagas;

import io.eventuate.customerservice.customermanagement.domain.CreditReservationDetails;
import io.eventuate.customerservice.customermanagement.domain.CustomerId;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.customerservice.customermanagement.domain.RejectionReason;
import io.eventuate.customerservice.customermanagement.sagas.proxies.OtherServiceProxy;
import io.eventuate.otherservice.othersubdomain.commandapi.InventoryOutOfStock;
import io.eventuate.otherservice.othersubdomain.commandapi.ReserveInventoryCommand;
import io.eventuate.examples.common.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.eventuate.tram.sagas.testing.SagaUnitTestSupport.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ReserveCreditSagaTest {

    private CustomerManagementService customerManagementService;
    private OtherServiceProxy otherServiceProxy;

    private CustomerId customerId = CustomerId.generate();
    private Long orderId = 102L;
    private Money orderTotal = new Money("12.34");

    @BeforeEach
    public void setUp() {
        customerManagementService = mock(CustomerManagementService.class);
        otherServiceProxy = new OtherServiceProxy();
    }

    private ReserveCreditSaga makeSaga() {
        return new ReserveCreditSaga(customerManagementService, otherServiceProxy);
    }

    @Test
    public void shouldReserveCreditSuccessfully() {
        ReserveCreditSagaData sagaData = new ReserveCreditSagaData(customerId, orderId, orderTotal);

        given()
            .saga(makeSaga(), sagaData)
            .expect()
            .command(new ReserveInventoryCommand(orderId))
            .to("otherService")
            .andGiven()
            .successReply()
            .expectCompletedSuccessfully();

        CreditReservationDetails expectedDetails = new CreditReservationDetails(customerId, orderId, orderTotal);
        verify(customerManagementService).noteCreditReservationPending(expectedDetails);
        verify(customerManagementService).noteCreditReservationApproved(expectedDetails);
    }

    @Test
    public void shouldRejectWhenInventoryOutOfStock() {
        ReserveCreditSagaData sagaData = new ReserveCreditSagaData(customerId, orderId, orderTotal);

        given()
            .saga(makeSaga(), sagaData)
            .expect()
            .command(new ReserveInventoryCommand(orderId))
            .to("otherService")
            .andGiven()
            .failureReply(new InventoryOutOfStock())
            .expectRolledBack()
            .assertSagaData(data ->
                assertThat(data.getRejectionReason()).isEqualTo(RejectionReason.OUT_OF_STOCK));

        CreditReservationDetails expectedDetails = new CreditReservationDetails(customerId, orderId, orderTotal);
        verify(customerManagementService).noteCreditReservationPending(expectedDetails);
        verify(customerManagementService).noteCreditReservationRejected(expectedDetails, RejectionReason.OUT_OF_STOCK);
    }
}
