package io.eventuate.customerservice.customermanagement.messaging;

import io.eventuate.customerservice.customermanagement.api.messaging.commands.ReserveCreditCommand;
import io.eventuate.customerservice.customermanagement.api.messaging.replies.CustomerCreditLimitExceeded;
import io.eventuate.customerservice.customermanagement.api.messaging.replies.CustomerCreditReserved;
import io.eventuate.customerservice.customermanagement.api.messaging.replies.CustomerNotFound;
import io.eventuate.customerservice.customermanagement.domain.CustomerCreditLimitExceededException;
import io.eventuate.customerservice.customermanagement.domain.CustomerId;
import io.eventuate.customerservice.customermanagement.domain.CustomerNotFoundException;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.annotations.EventuateCommandHandler;
import io.eventuate.tram.messaging.common.Message;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withFailure;
import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;

public class CustomerCommandHandler {

  private CustomerManagementService customerManagementService;

  public CustomerCommandHandler(CustomerManagementService customerManagementService) {
    this.customerManagementService = customerManagementService;
  }


  @EventuateCommandHandler(subscriberId="customerCommandDispatcher", channel="customerService")
  public Message reserveCredit(CommandMessage<ReserveCreditCommand> cm) {
    ReserveCreditCommand cmd = cm.getCommand();
    try {
      customerManagementService.reserveCredit(new CustomerId(cmd.customerId()), cmd.orderId(), cmd.orderTotal());
      return withSuccess(new CustomerCreditReserved());
    } catch (CustomerNotFoundException e) {
      return withFailure(new CustomerNotFound());
    } catch (CustomerCreditLimitExceededException e) {
      return withFailure(new CustomerCreditLimitExceeded());
    }
  }

}
