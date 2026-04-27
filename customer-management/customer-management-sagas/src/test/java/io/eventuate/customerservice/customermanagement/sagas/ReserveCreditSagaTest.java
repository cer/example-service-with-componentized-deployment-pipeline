package io.eventuate.customerservice.customermanagement.sagas;

import io.eventuate.customerservice.customermanagement.api.messaging.commands.ReserveCreditCommand;
import io.eventuate.customerservice.customermanagement.api.messaging.replies.CustomerCreditLimitExceeded;
import io.eventuate.customerservice.customermanagement.api.messaging.replies.CustomerNotFound;
import io.eventuate.customerservice.customermanagement.domain.CreditReservationDetails;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.customerservice.customermanagement.domain.RejectionReason;
import io.eventuate.customerservice.customermanagement.sagas.proxies.CustomerServiceProxy;
import io.eventuate.examples.common.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.eventuate.tram.sagas.testing.SagaUnitTestSupport.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ReserveCreditSagaTest {

    private CustomerManagementService customerManagementService;
    private CustomerServiceProxy customerServiceProxy;

    private Long customerId = 101L;
    private Long orderId = 102L;
    private Money orderTotal = new Money("12.34");

    @BeforeEach
    public void setUp() {
        customerManagementService = mock(CustomerManagementService.class);
        customerServiceProxy = new CustomerServiceProxy();
    }

    private ReserveCreditSaga makeSaga() {
        return new ReserveCreditSaga(customerManagementService, customerServiceProxy);
    }

    @Test
    public void shouldReserveCreditSuccessfully() {
        ReserveCreditSagaData sagaData = new ReserveCreditSagaData(customerId, orderId, orderTotal);

        given()
            .saga(makeSaga(), sagaData)
            .expect()
            .command(new ReserveCreditCommand(customerId, orderId, orderTotal))
            .to("customerService")
            .andGiven()
            .successReply()
            .expectCompletedSuccessfully();

        CreditReservationDetails expectedDetails = new CreditReservationDetails(customerId, orderId, orderTotal);
        verify(customerManagementService).noteCreditReservationPending(expectedDetails);
        verify(customerManagementService).noteCreditReservationApproved(expectedDetails);
    }

    @Test
    public void shouldRejectWhenCustomerNotFound() {
        ReserveCreditSagaData sagaData = new ReserveCreditSagaData(customerId, orderId, orderTotal);

        given()
            .saga(makeSaga(), sagaData)
            .expect()
            .command(new ReserveCreditCommand(customerId, orderId, orderTotal))
            .to("customerService")
            .andGiven()
            .failureReply(new CustomerNotFound())
            .expectRolledBack()
            .assertSagaData(data ->
                assertThat(data.getRejectionReason()).isEqualTo(RejectionReason.UNKNOWN_CUSTOMER));

        CreditReservationDetails expectedDetails = new CreditReservationDetails(customerId, orderId, orderTotal);
        verify(customerManagementService).noteCreditReservationPending(expectedDetails);
        verify(customerManagementService).noteCreditReservationRejected(expectedDetails, RejectionReason.UNKNOWN_CUSTOMER);
    }

    @Test
    public void shouldRejectWhenCreditLimitExceeded() {
        ReserveCreditSagaData sagaData = new ReserveCreditSagaData(customerId, orderId, orderTotal);

        given()
            .saga(makeSaga(), sagaData)
            .expect()
            .command(new ReserveCreditCommand(customerId, orderId, orderTotal))
            .to("customerService")
            .andGiven()
            .failureReply(new CustomerCreditLimitExceeded())
            .expectRolledBack()
            .assertSagaData(data ->
                assertThat(data.getRejectionReason()).isEqualTo(RejectionReason.INSUFFICIENT_CREDIT));

        CreditReservationDetails expectedDetails = new CreditReservationDetails(customerId, orderId, orderTotal);
        verify(customerManagementService).noteCreditReservationPending(expectedDetails);
        verify(customerManagementService).noteCreditReservationRejected(expectedDetails, RejectionReason.INSUFFICIENT_CREDIT);
    }
}
